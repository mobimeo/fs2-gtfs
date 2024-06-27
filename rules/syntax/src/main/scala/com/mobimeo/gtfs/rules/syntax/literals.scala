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

import cats.syntax.all._
import com.mobimeo.gtfs.rules._
import org.typelevel.literally.Literally
import scala.quoted.{Expr => QExpr, *}
import cats.data.NonEmptyList
import fs2.Pure

object literals {

  extension (inline ctx: StringContext) {
    inline def ruleset(inline args: Any*): RuleSet[Pure] =
      ${ RuleSetLiteral('ctx, 'args) }
    inline def rule(inline args: Any*): Rule[Pure] =
      ${ RuleLiteral('ctx, 'args) }
    inline def matcher(inline args: Any*): Matcher =
      ${ MatcherLiteral('ctx, 'args) }
    inline def action(inline args: Any*): Action[Pure] =
      ${ ActionLiteral('ctx, 'args) }
    inline def transform(inline args: Any*): Transformation[Pure] =
      ${ TransformationLiteral('ctx, 'args) }
  }

  given [T](using ToExpr[T], Type[T]): ToExpr[NonEmptyList[T]] with {
    def apply(nel: NonEmptyList[T])(using Quotes) = nel match {
      case NonEmptyList(t, Nil)  => '{ NonEmptyList.one(${ QExpr(t) }) }
      case NonEmptyList(t, tail) => '{ NonEmptyList(${ QExpr(t) }, ${ QExpr(tail) }) }
    }
  }

  given ToExpr[Value] with {
    def apply(v: Value)(using Quotes) = v match {
      case Value.Str(s)        => '{ Value.Str(${ QExpr(s) }) }
      case Value.Field(fld)    => '{ Value.Field(${ QExpr(fld) }) }
      case Value.Context(path) => '{ Value.Context(${ QExpr(path) }) }
    }
  }

  given ToExpr[Variable] with {
    def apply(v: Variable)(using Quotes) = v match {
      case Value.Field(fld)    => '{ Value.Field(${ QExpr(fld) }) }
      case Value.Context(path) => '{ Value.Context(${ QExpr(path) }) }
    }
  }

  given ToExpr[Matcher] with {
    def apply(m: Matcher)(using Quotes) = m match {
      case Matcher.Any              => '{ Matcher.Any }
      case Matcher.Exists(fld)      => '{ Matcher.Exists(${ QExpr(fld) }) }
      case Matcher.Equals(l, r)     => '{ Matcher.Equals(${ QExpr(l) }, ${ QExpr(r) }) }
      case Matcher.In(fld, vs)      => '{ Matcher.In(${ QExpr(fld) }, ${ QExpr(vs) }) }
      case Matcher.Matches(fld, re) => '{ Matcher.Matches(${ QExpr(fld) }, ${ QExpr(re) }) }
      case Matcher.Not(inner)       => '{ Matcher.Not(${ QExpr(inner) }) }
      case Matcher.And(l, r)        => '{ Matcher.And(${ QExpr(l) }, ${ QExpr(r) }) }
      case Matcher.Or(l, r)         => '{ Matcher.Or(${ QExpr(l) }, ${ QExpr(r) }) }
    }
  }

  given ToExpr[Expr[Pure]] with {
    def apply(e: Expr[Pure])(using Quotes) = e match {
      case Expr.Val(v)                    => '{ Expr.Val(${ QExpr(v) }) }
      case Expr.NamedFunction(name, args) => '{ Expr.NamedFunction(${ QExpr(name) }, ${ QExpr(args) }) }
      case Expr.AnonymousFunction(_, _)   => '{ compiletime.error("anonymous functions not supported") }
    }
  }

  given ToExpr[LogLevel] with {
    def apply(l: LogLevel)(using Quotes) = l match {
      case LogLevel.Debug   => '{ LogLevel.Debug }
      case LogLevel.Info    => '{ LogLevel.Info }
      case LogLevel.Warning => '{ LogLevel.Warning }
      case LogLevel.Error   => '{ LogLevel.Error }
    }
  }

  given ToExpr[Transformation[Pure]] with {
    def apply(t: Transformation[Pure])(using Quotes) = t match {
      case Transformation.Set(fld, e) => '{ Transformation.Set(${ QExpr(fld) }, ${ QExpr(e) }) }
      case Transformation.SearchAndReplace(fld, re, repl) =>
        '{ Transformation.SearchAndReplace(${ QExpr(fld) }, ${ QExpr(re) }, ${ QExpr(repl) }) }
    }
  }

  given ToExpr[Action[Pure]] with {
    def apply(a: Action[Pure])(using Quotes) = a match {
      case Action.Delete()      => '{ Action.Delete() }
      case Action.Log(msg, lvl) => '{ Action.Log(${ QExpr(msg) }, ${ QExpr(lvl) }) }
      case Action.Transform(ts) => '{ Action.Transform(${ QExpr(ts) }) }
    }
  }

  given ToExpr[Rule[Pure]] with {
    def apply(r: Rule[Pure])(using Quotes) =
      '{ Rule(${ QExpr(r.name) }, ${ QExpr(r.matcher) }, ${ QExpr(r.action) }) }
  }

  given ToExpr[RuleSet[Pure]] with {
    def apply(r: RuleSet[Pure])(using Quotes) =
      '{ RuleSet(${ QExpr(r.file) }, ${ QExpr(r.rules) }, ${ QExpr(r.additions) }) }
  }

  object RuleSetLiteral extends Literally[RuleSet[Pure]] {
    def validate(s: String)(using Quotes) =
      RuleParser.ruleset
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => Expr(r))
  }

  object RuleLiteral extends Literally[Rule[Pure]] {
    def validate(s: String)(using Quotes) =
      RuleParser.rule
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => Expr(r))
  }

  object MatcherLiteral extends Literally[Matcher] {
    def validate(s: String)(using Quotes) =
      RuleParser.matcher
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => Expr(r))
  }

  object ActionLiteral extends Literally[Action[Pure]] {
    def validate(s: String)(using Quotes) =
      RuleParser.action
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => Expr(r))
  }

  object TransformationLiteral extends Literally[Transformation[Pure]] {
    def validate(s: String)(using Quotes) =
      RuleParser.transformation
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => Expr(r))
  }

}
