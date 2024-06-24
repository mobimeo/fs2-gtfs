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

package com.mobimeo.gtfs.rules

import cats.data.NonEmptyList
import cats.Show
import cats.syntax.all._

case class TypedRuleSet[T](rules: T => List[TypedRule[T]])

case class TypedRule[T](matcher: TypedMatcher, action: TypedAction[T])

case class TypedMatcher(predicate: Boolean)
case class TypedAction[T](result: T)

case class RuleSet[+F[_]](file: String, rules: List[Rule[F]], additions: List[NonEmptyList[String]])

case class Rule[+F[_]](name: String, matcher: Matcher, action: Action[F])

sealed trait Matcher {
  def unary_! : Matcher =
    this match {
      case Matcher.Not(inner) => inner
      case _                  => Matcher.Not(this)
    }

  def and(that: Matcher): Matcher = (this, that) match {
    case (Matcher.Any, _)                     => that
    case (_, Matcher.Any)                     => this
    case (Matcher.Not(in1), Matcher.Not(in2)) => !(in1.or(in2))
    case _                                    => Matcher.And(this, that)
  }

  def or(that: Matcher): Matcher = (this, that) match {
    case (Matcher.Any, _)                     => Matcher.Any
    case (_, Matcher.Any)                     => Matcher.Any
    case (Matcher.Not(in1), Matcher.Not(in2)) => !(in1.and(in2))
    case _                                    => Matcher.Or(this, that)
  }
}

object Matcher {
  case object Any                                  extends Matcher
  case class Equals(left: Value, right: Value)     extends Matcher
  case class In(value: Value, values: List[Value]) extends Matcher
  case class Matches(value: Value, regex: String)  extends Matcher
  case class Exists(variable: Variable)            extends Matcher

  case class And(left: Matcher, right: Matcher) extends Matcher
  case class Or(left: Matcher, right: Matcher)  extends Matcher
  case class Not(inner: Matcher)                extends Matcher
}

sealed trait Value {
  def in(values: List[Value]): Matcher = Matcher.In(this, values)
  def matches(regex: String): Matcher  = Matcher.Matches(this, regex)
  def ===(that: Value): Matcher        = Matcher.Equals(this, that)
}
sealed trait Variable extends Value {
  def exists: Matcher = Matcher.Exists(this)
}
object Value {
  case class Str(value: String)                 extends Value
  case class Field(name: Value)                 extends Variable
  case class Context(keys: NonEmptyList[Value]) extends Variable

  implicit object show extends Show[Value] {
    def show(t: Value): String =
      t match {
        case Str(v)        => s""""${v.replace("\"", "\\\"")}""""
        case Field(name)   => s"row[${show(name)}]"
        case Context(keys) => s"ctx${keys.map(show(_)).mkString_("[", "][", "]")}"
      }
  }
}

sealed trait Action[+F[_]]
object Action {

  case class Delete[F[_]]() extends Action[F]

  case class Log[F[_]](msg: Expr[F], level: LogLevel) extends Action[F]

  case class Transform[+F[_]](transformations: NonEmptyList[Transformation[F]]) extends Action[F]

}

sealed trait Transformation[+F[_]]
object Transformation {

  case class Set[F[_]](field: Value, to: Expr[F])                                     extends Transformation[F]
  case class SearchAndReplace[F[_]](field: Value, regex: String, replacement: String) extends Transformation[F]

}

sealed trait Expr[+F[_]]
object Expr {
  case class NamedFunction[F[_]](name: String, args: List[Expr[F]])                     extends Expr[F]
  case class AnonymousFunction[F[_]](f: List[String] => F[String], args: List[Expr[F]]) extends Expr[F]
  case class Val[F[_]](v: Value)                                                        extends Expr[F]

  implicit def show[F[_]]: Show[Expr[F]] =
    new Show[Expr[F]] {
      override def show(t: Expr[F]): String = t match {
        case NamedFunction(name, args)  => show"$name(${args.map(show).mkString(", ")})"
        case AnonymousFunction(_, args) => show"<function>(${args.map(show).mkString(", ")})"
        case Val(v)                     => v.show
      }
    }
}
