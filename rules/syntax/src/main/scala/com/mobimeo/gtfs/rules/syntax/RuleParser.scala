/*
 * Copyright 2021 Mobimeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobimeo.gtfs.rules.syntax

import com.mobimeo.gtfs.rules._

import cats.parse._
import cats.syntax.all._
import cats.data.NonEmptyList
import fs2.Pure

/** Concrete syntax parser for the rule DSL. The parsed grammar is defined as follows:
  * {{{
  * ident = [a-zA-Z] [a-zA-Z0-9_-]*
  * str = '"' <any character with " and \ escaped> '"'
  * regex = '/' <any character with / and \ escaped> '/'
  *
  * file = ruleset*
  *
  * ruleset = 'ruleset' 'for' filename '{' rule ('or' rule)* '}'
  *
  * filename = 'agency'
  *           | 'stops'
  *           | 'routes'
  *           | 'trips'
  *           | 'stop_times'
  *           | 'calendar'
  *           | 'calendar_dates'
  *           | 'fare_attributes'
  *           | 'fare_rules'
  *           | 'shapes'
  *           | 'frequencies'
  *           | 'transfers'
  *           | 'pathways'
  *           | 'levels'
  *           | 'feed_info'
  *           | 'translations'
  *           | 'attributions'
  *
  * rule = 'rule' ident '{' 'when' matcher 'then' action '}'
  *
  * matcher = '(' matcher ')'
  *         | '!' factor
  *         | 'any'
  *         | variable 'exists'
  *         | value valueMatcher
  *         | matcher 'and' matcher
  *         | matcher 'or' matcher
  *
  * valueMatcher = '==' value
  *               | 'in' '[' value* ']'
  *               | 'matches' regex
  *
  * action = 'delete'
  *         | transformation ('then' transformation)*
  *         | log
  *
  * transformation = 'set' 'row' '[' str ']' 'to' value
  *                 | 'search' regex 'in' 'row' '[' str ']' 'and' 'replace' 'by' value
  *
  * log = 'debug' '(' expr ')'
  *     | 'info' '(' expr ')'
  *     | 'warning' '(' expr ')'
  *     | 'error' '(' expr ')'
  *
  * expr = ident '(' (expr (',' expr)*)? ')'
  *       | value
  *       | expr + expr
  *
  * value = str
  *       | variable
  *
  * variable = 'row' '[' value ']'
  *           | 'ctx' '[' value ']' ('[' value ']')*
  * }}}
  *
  * For matchers, operators have natural precendence: `!` has precedence over `and` which as precedence over `or`. This
  * means that `!m1 and m2 or m3` is equivalent to `((!m1) and m2) or m3`. Combinators are left associative.
  */
object RuleParser {

  import Parser._

  private val whitespace: Parser[Unit]    = oneOf(List(charIn(" \t\r\n"), string("//") ~ charsWhile(_ != '\n'))).void
  private val whitespaces0: Parser0[Unit] = whitespace.rep0.void

  private val identStart = ('a' to 'z') ++ ('A' to 'Z')
  private val identChar  = identStart ++ ('0' to '9') ++ List('-', '_')

  private val sel = defer(value).between(ch('['), ch(']'))

  private val ident: Parser[String] =
    (peek(charIn(identStart)).with1 *> charsWhile(identChar.contains(_)))
      .withContext("identifier")
      .surroundedBy(whitespaces0)

  private def keyword(kw: String) =
    (string(kw) <* not(
      peek(charWhere(c => c >= 'a' && c <= 'z' && c != '_'))
    )).surroundedBy(whitespaces0).void

  private def ch(c: Char): Parser[Unit] =
    char(c).surroundedBy(whitespaces0)

  private val filename: Parser[String] = stringIn(
    List(
      "agency",
      "stops",
      "routes",
      "trips",
      "stop_times",
      "calendar",
      "calendar_dates",
      "fare_attributes",
      "fare_rules",
      "shapes",
      "frequencies",
      "transfers",
      "pathways",
      "levels",
      "feed_info",
      "translations",
      "attributions"
    )
  ).surroundedBy(whitespaces0)

  private val str: Parser[String] =
    (char('"') *> oneOf(
      List(charsWhile(!"\\\"".contains(_)).string, char('\\') *> oneOf0(List(char('"').string, pure("\\"))))
    ).rep0.map(_.combineAll) <* char('"')).surroundedBy(whitespaces0)

  private val regex: Parser[String] =
    (char('/') *> oneOf(
      List(charsWhile(!"\\/".contains(_)).string, char('\\') *> oneOf0(List(char('/').string, pure("\\"))))
    ).rep.map(_.combineAll) <* char('/')).surroundedBy(whitespaces0)

  private val variable: Parser[Variable] =
    oneOf(List(keyword("row") *> sel.map(Value.Field(_)), keyword("ctx") *> sel.rep.map(Value.Context(_))))

  private val value: Parser[Value] =
    oneOf(
      List(
        str.map(Value.Str(_)),
        variable
      )
    )

  private val list: Parser[List[Value]] = repSep0(value, min = 0, sep = ch(',')).with1.between(ch('['), ch(']'))

  val matcher: Parser[Matcher] = Parser.recursive[Matcher] { matcher =>
    val genericMatcher: Parser[Value => Matcher] = oneOf(
      List(
        string("==").surroundedBy(whitespaces0) *> value.map(v => Matcher.Equals(_, v)),
        keyword("in") *> list.map(vs => Matcher.In(_, vs)),
        keyword("matches") *> regex.surroundedBy(whitespaces0).map(re => Matcher.Matches(_, re))
      )
    )

    val factorMatcher: Parser[Matcher] =
      oneOf(
        List(
          (variable ~
            oneOf(
              List(
                keyword("exists").as(Matcher.Exists(_)),
                genericMatcher.map(f => (v: Variable) => f(v))
              )
            )).map { case (v, f) => f(v) },
          (str.map(Value.Str(_)) ~ genericMatcher).map { case (v, f) => f(v) }
        )
      )

    val factor = Parser.recursive[Matcher] { factor =>
      oneOf(
        List(
          char('(') *> matcher <* char(')'),
          char('!') *> factor.map(!_),
          string("any").as(Matcher.Any),
          factorMatcher
        )
      )
    }

    val term = repSep(factor.surroundedBy(whitespaces0), min = 1, sep = string("and")).map { fs =>
      fs.reduceLeft(_.and(_))
    }

    repSep(term.surroundedBy(whitespaces0), min = 1, sep = string("or")).map(ts => ts.reduceLeft(_.or(_)))

  }

  val expr: Parser[Expr[Pure]] = Parser.recursive[Expr[Pure]] { expr =>
    val paramList: Parser[List[Expr[Pure]]] = repSep0(expr, min = 0, sep = char(',')).with1.between(ch('('), ch(')'))
    val factor: Parser[Expr[Pure]] = oneOf(
      List(
        value.map(Expr.Val(_)),
        (ident ~ paramList).map { case (f, args) => Expr.NamedFunction(f, args) }
      )
    )

    factor.repSep(ch('+')).map {
      case NonEmptyList(e, Nil) => e
      case args                 => Expr.NamedFunction("concat", args.toList)
    }
  }

  val transformation: Parser[Transformation[Pure]] =
    oneOf(
      List(
        ((string("search") *> regex) ~ (keyword("in") *> keyword("row") *> sel) ~ (keyword("and") *> keyword(
          "replace"
        ) *> keyword(
          "by"
        ) *> str)).map { case ((regex, field), repl) =>
          Transformation.SearchAndReplace(field, regex, repl)
        },
        ((string("set") *> keyword("row") *> sel) ~ (keyword("to") *> expr)).map { case (field, to) =>
          Transformation.Set(field, to)
        }
      )
    )

  def transformations: Parser[NonEmptyList[Transformation[Pure]]] =
    repSep(transformation.surroundedBy(whitespaces0), sep = string("then"))

  val log: Parser[(LogLevel, Expr[Pure])] =
    stringIn(List("debug", "info", "warning", "error"))
      .surroundedBy(whitespaces0)
      .map(LogLevel.withNameInsensitive((_))) ~ expr.between(ch('('), ch(')'))

  val action: Parser[Action[Pure]] =
    oneOf(
      List(
        string("delete").as(Action.Delete()),
        log.map { case (level, msg) => Action.Log(msg, level) },
        transformations.map(Action.Transform(_))
      )
    )

  val rule: Parser[Rule[Pure]] =
    (string("rule") *> ident ~ (keyword("when") *> matcher
      .surroundedBy(whitespaces0) ~ (string("then") *> action.surroundedBy(whitespaces0))).between(ch('{'), ch('}')))
      .map { case (name, (matcher, action)) =>
        Rule(name, matcher, action)
      }

  val ruleset: Parser[RuleSet[Pure]] =
    string("ruleset") *> keyword("for") *> (filename ~ (ch('{') *> repSep0(
      rule.surroundedBy(whitespaces0),
      min = 0,
      sep = string("or")
    ) <* ch('}')))
      .map { case (name, rules) =>
        RuleSet(s"$name.txt", rules, Nil)
      }

  val file: Parser0[List[RuleSet[Pure]]] =
    start *> whitespaces0 *> ruleset.rep0 <* whitespaces0 <* end

}
