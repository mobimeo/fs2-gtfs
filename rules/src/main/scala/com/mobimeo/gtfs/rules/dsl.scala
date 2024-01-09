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
import com.mobimeo.gtfs.StandardName
import scala.annotation.tailrec
import LogLevel

class Dsl[F[_]] {

  object ctx {
    def apply(name: Value): CtxBuilder           = new CtxBuilder(NonEmptyList.one(name))
    def apply(name: String): CtxBuilder          = new CtxBuilder(NonEmptyList.one(Value.Str(name)))
    def apply(name: CtxBuilder): CtxBuilder      = new CtxBuilder(NonEmptyList.one(Value.Context(name.path.reverse)))
    def apply(name: RowFieldBuilder): CtxBuilder = new CtxBuilder(NonEmptyList.one(Value.Field(name.name)))
  }

  class CtxBuilder private[Dsl] (val path: NonEmptyList[Value]) {
    def apply(name: Value): CtxBuilder = new CtxBuilder(name :: path)
    def exists: Matcher                = Matcher.Exists(Value.Context(path.reverse))
  }

  object row {
    def apply(name: Value): RowFieldBuilder  = new RowFieldBuilder(name)
    def apply(name: String): RowFieldBuilder = new RowFieldBuilder(Value.Str(name))
  }
  class RowFieldBuilder private[Dsl] (val name: Value) {
    def exists: Matcher = Matcher.Exists(Value.Field(name))

    def :=(v: Expr[F]): Transformation[F] = Transformation.Set(name, v)
  }

  class RuleBuilder(name: String) {
    def when(matcher: Matcher): TransformationBuilder = new TransformationBuilder(name, matcher)
  }

  class TransformationBuilder(name: String, matcher: Matcher) {
    def perform(t: Transformation[F], ts: Transformation[F]*): RulesBuilder =
      RulesBuilder.Apply(name, matcher, NonEmptyList.of(t, ts: _*))
    def delete: RulesBuilder = RulesBuilder.Delete(name, matcher)
    def log(msg: Expr[F], at: LogLevel = LogLevel.Info): RulesBuilder =
      RulesBuilder.Log(name, matcher, msg, at)
    def debug(msg: Expr[F]): RulesBuilder =
      RulesBuilder.Log(name, matcher, msg, LogLevel.Debug)
    def info(msg: Expr[F]): RulesBuilder =
      RulesBuilder.Log(name, matcher, msg, LogLevel.Info)
    def warning(msg: Expr[F]): RulesBuilder =
      RulesBuilder.Log(name, matcher, msg, LogLevel.Warning)
    def error(msg: Expr[F]): RulesBuilder =
      RulesBuilder.Log(name, matcher, msg, LogLevel.Error)
  }

  sealed abstract class RulesBuilder {
    def build: List[Rule[F]]
    def orElse(that: RulesBuilder): RulesBuilder =
      (this, that) match {
        case (RulesBuilder.Single(rule), RulesBuilder.ListBuilder(rules)) => RulesBuilder.ListBuilder(rule :: rules)
        case (RulesBuilder.ListBuilder(rules), RulesBuilder.Single(rule)) => RulesBuilder.ListBuilder(rules :+ rule)
        case (_, _) => RulesBuilder.ListBuilder(this.build ++ that.build)
      }
  }

  private object RulesBuilder {
    object Single {
      def unapply(builder: RulesBuilder): Option[Rule[F]] =
        builder.build match {
          case List(rule) => Some(rule)
          case _          => None
        }
    }
    case class Apply(name: String, matcher: Matcher, ts: NonEmptyList[Transformation[F]]) extends RulesBuilder {
      def build: List[Rule[F]] = List(Rule(name, matcher, Action.Transform(ts)))
    }
    case class Delete(name: String, matcher: Matcher) extends RulesBuilder {
      def build: List[Rule[F]] = List(Rule(name, matcher, Action.Delete()))
    }
    case class Log(name: String, matcher: Matcher, msg: Expr[F], at: LogLevel) extends RulesBuilder {
      def build: List[Rule[F]] = List(Rule(name, matcher, Action.Log(msg, at)))
    }
    case class ListBuilder(rules: List[Rule[F]]) extends RulesBuilder {
      def build: List[Rule[F]] = rules
    }
  }

  def ruleset(file: StandardName)(rules: RulesBuilder): RuleSet[F] = ruleset(file.entryName)(rules)

  def ruleset(file: String)(rules: RulesBuilder): RuleSet[F] = RuleSet(file, rules.build, Nil)

  def rule(name: String): RuleBuilder = new RuleBuilder(name)

  val any: Matcher = Matcher.Any

  implicit def strToValue(s: String): Value =
    Value.Str(s)

  implicit def rowBuilderToValue(b: RowFieldBuilder): Value =
    Value.Field(b.name)

  implicit def ctxBuilderToValue(b: CtxBuilder): Value =
    Value.Context(b.path.reverse)

  implicit def strToExpr(s: String): Expr[F] =
    Expr.Val(Value.Str(s))

  implicit def valueToExpr(v: Value): Expr[F] =
    Expr.Val(v)

  implicit def rowBuilderToExpr(b: RowFieldBuilder): Expr[F] =
    Expr.Val(Value.Field(b.name))

  implicit def ctxBuilderToExpr(b: CtxBuilder): Expr[F] =
    Expr.Val(Value.Context(b.path.reverse))

  def call(name: String)(args: Expr[F]*): Expr[F] =
    Expr.NamedFunction(name, args.toList)

  def call(f: List[String] => F[String])(args: Expr[F]*): Expr[F] =
    Expr.AnonymousFunction(f, args.toList)

  def in(name: String): InFieldBuilder =
    new InFieldBuilder(name)

  class InFieldBuilder(field: String) {
    def search(regex: String): SearchAndReplaceBuilder =
      new SearchAndReplaceBuilder(Value.Str(field), regex)
  }

  class SearchAndReplaceBuilder(field: Value, regex: String) {
    def andReplaceBy(value: String): Transformation[F] = Transformation.SearchAndReplace(field, regex, value)
  }

  def concat(es: Expr[F]*): Expr[F] =
    Expr.NamedFunction("concat", es.toList)

  def uppercase(es: Expr[F]*): Expr[F] =
    Expr.NamedFunction("uppercase", es.toList)

  def lowercase(es: Expr[F]*): Expr[F] =
    Expr.NamedFunction("lowercase", es.toList)

  def trim(es: Expr[F]*): Expr[F] =
    Expr.NamedFunction("trim", es.toList)

  implicit class Interpolators(val sc: StringContext) {
    def concat(args: Expr[F]*): Expr[F] = {
      @tailrec
      def loop(parts: List[String], args: List[Expr[F]], acc: List[Expr[F]]): List[Expr[F]] = (parts, args) match {
        case (p :: parts, a :: args) =>
          loop(parts, args, a :: Expr.Val[F](Value.Str(p)) :: acc)
        case ("" :: parts, Nil) =>
          loop(parts, Nil, acc)
        case (p :: parts, Nil) =>
          loop(parts, Nil, Expr.Val[F](Value.Str(p)) :: acc)
        case (Nil, _) =>
          acc.reverse
      }
      Expr.NamedFunction("concat", loop(sc.parts.toList, args.toList, Nil))
    }
  }
}
