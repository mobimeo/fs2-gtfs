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

import cats.Show
import cats.syntax.all._
import fs2.Pure

/** Pretty prints rules in the concrete syntax.
  * '''Note: ''' anonymous functions cannot be rendered as then include scala code. Instances are printed as concatenation of the arguments.
  */
object pretty {

  private def escapeRe(re: String) = re.replace("/", "\\/")

  private def escapeString(s: String) = s.replace("\"", "\\\"")

  private def priority(m: Matcher) =
    m match {
      case Matcher.Any           => 0
      case Matcher.Equals(_, _)  => 0
      case Matcher.In(_, _)      => 0
      case Matcher.Matches(_, _) => 0
      case Matcher.Exists(_)     => 0
      case Matcher.And(_, _)     => 2
      case Matcher.Or(_, _)      => 3
      case Matcher.Not(_)        => 1
    }

  private def parenthesize(m: Matcher, withinPrio: Int, onRight: Boolean): String = {
    val prio = priority(m)
    if (prio > withinPrio || (prio == withinPrio && onRight))
      // lesser priority, we need to parenthesize
      // things being left associative, we need also to parenthesize in case
      // we are on the right part at same priority
      show"($m)"
    else
      show"$m"
  }

  implicit object value extends Show[Value] {
    def show(t: Value): String =
      t match {
        case Value.Str(v)        => s""""${escapeString(v)}""""
        case Value.Field(name)   => show"""row[$name]"""
        case Value.Context(keys) => show"ctx${keys.mkString_("[", "][", "]")}"
      }
  }

  implicit val expr: Show[Expr[Pure]] =
    Show.show {
      case Expr.NamedFunction(name, args)  => show"$name(${args.mkString_(", ")})"
      case Expr.AnonymousFunction(_, args) => args.mkString_(" + ")
      case Expr.Val(v)                     => v.show
    }
  implicit val matcher: Show[Matcher] = Show.show { m =>
    val prio = priority(m)
    m match {
      case Matcher.Any            => "any"
      case Matcher.Exists(v)      => show"$v exists"
      case Matcher.Equals(l, r)   => show"$l == $r"
      case Matcher.Matches(v, re) => show"$v matches /${escapeRe(re)}/"
      case Matcher.In(v, vs)      => show"$v in ${vs.mkString_("[", ", ", "]")}"
      case Matcher.And(l, r)      => show"${parenthesize(l, prio, false)} and ${parenthesize(r, prio, true)}"
      case Matcher.Or(l, r)       => show"${parenthesize(l, prio, false)} or ${parenthesize(r, prio, true)}"
      case Matcher.Not(m)         => show"!${parenthesize(m, prio, false)}"
    }
  }

  implicit val transformation: Show[Transformation[Pure]] = Show.show {
    case Transformation.Set(fld, e) =>
      show"set row[$fld] to $e"
    case Transformation.SearchAndReplace(fld, re, repl) =>
      show"""search $re in row[$fld] and replace by "${escapeString(repl)}"""""
  }

  implicit val action: Show[Action[Pure]] = Show.show {
    case Action.Delete()                 => "delete"
    case Action.Log(e, LogLevel.Debug)   => show"debug($e)"
    case Action.Log(e, LogLevel.Info)    => show"info($e)"
    case Action.Log(e, LogLevel.Warning) => show"warning($e)"
    case Action.Log(e, LogLevel.Error)   => show"error($e)"
    case Action.Transform(ts)            => show"${ts.mkString_("\n  then ")}"
  }

  implicit val rule: Show[Rule[Pure]] = Show.show { r =>
    show"""rule ${r.name} {
          |  when ${r.matcher}
          |  then ${r.action}
          |}""".stripMargin
  }

  implicit val ruleset: Show[RuleSet[Pure]] = Show.show { rs =>
    show"""ruleset for ${rs.file.dropRight(4)} {
          |  ${rs.rules.map(_.show.replace("\n", "\n  ")).mkString_("\n  or\n  ")}
          |}""".stripMargin
  }

}
