package com.mobimeo.gtfs.rules.syntax

import org.typelevel.literally.Literally
import com.mobimeo.gtfs.rules._
import cats.syntax.all._
import cats.data.NonEmptyList
import scala.annotation.unused
import com.mobimeo.gtfs.rules
import scala.reflect.macros.blackbox.Context
import fs2.Pure

object literals {

  implicit class RulesInterpolator(val sc: StringContext) extends AnyVal {
    def ruleset(args: Any*): RuleSet[Pure] = macro RuleSetLiteral.make
    def rule(args: Any*): Rule[Pure] = macro RuleLiteral.make
    def matcher(args: Any*): Matcher = macro MatcherLiteral.make
    def action(args: Any*): Action[Pure] = macro ActionLiteral.make
    def transform(args: Any*): Transformation[Pure] = macro TransformationLiteral.make
  }

  trait LiftableImpls {
    val c: Context
    import c.universe._

    implicit def nel[T](implicit @unused T: Liftable[T]): Liftable[NonEmptyList[T]] = Liftable[NonEmptyList[T]] {
      case NonEmptyList(t, Nil)  => q"_root_.cats.data.NonEmptyList.one($t)"
      case NonEmptyList(t, tail) => q"_root_.cats.data.NonEmptyList($t, $tail)"
    }

    implicit lazy val value: Liftable[Value] = Liftable[Value] {
      case Value.Str(s)        => q"_root_.com.mobimeo.gtfs.rules.Value.Str($s)"
      case Value.Field(name)   => q"_root_.com.mobimeo.gtfs.rules.Value.Field($name)"
      case Value.Context(path) => q"_root_.com.mobimeo.gtfs.rules.Value.Context($path)"
    }

    implicit lazy val variable: Liftable[Variable] = Liftable[Variable] {
      case Value.Field(name)   => q"_root_.com.mobimeo.gtfs.rules.Value.Field($name)"
      case Value.Context(path) => q"_root_.com.mobimeo.gtfs.rules.Value.Context($path)"
    }

    implicit lazy val matcher: Liftable[Matcher] = Liftable[Matcher] {
      case Matcher.Any =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Any"
      case Matcher.Equals(left, right) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Equals($left, $right)"
      case Matcher.Exists(v) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Exists($v)"
      case Matcher.In(v, vs) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.In($v, $vs)"
      case Matcher.Matches(v, re) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Matches($v, $re)"
      case Matcher.Not(inner) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Not($inner)"
      case Matcher.And(left, right) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.And($left, $right)"
      case Matcher.Or(left, right) =>
        q"_root_.com.mobimeo.gtfs.rules.Matcher.Or($left, $right)"
    }

    implicit lazy val expr: Liftable[rules.Expr[Pure]] = Liftable[rules.Expr[Pure]] {
      case rules.Expr.Val(v) =>
        q"_root_.com.mobimeo.gtfs.rules.Expr.Val[_root_.fs2.Pure]($v)"
      case rules.Expr.NamedFunction(name, args) =>
        q"_root_.com.mobimeo.gtfs.rules.Expr.NamedFunction[_root_.fs2.Pure]($name, $args)"
      case rules.Expr.AnonymousFunction(_, _) =>
        c.abort(c.enclosingPosition, "anonymous functions are not supported")
    }

    implicit lazy val loglevel: Liftable[LogLevel] = Liftable[LogLevel] {
      case LogLevel.Debug   => q"_root_.com.mobimeo.gtfs.rules.LogLevel.Debug"
      case LogLevel.Info    => q"_root_.com.mobimeo.gtfs.rules.LogLevel.Info"
      case LogLevel.Warning => q"_root_.com.mobimeo.gtfs.rules.LogLevel.Warning"
      case LogLevel.Error   => q"_root_.com.mobimeo.gtfs.rules.LogLevel.Error"
    }

    implicit lazy val transformation: Liftable[Transformation[Pure]] = Liftable[Transformation[Pure]] {
      case Transformation.Set(v, e) => q"_root_.com.mobimeo.gtfs.rules.Transformation.Set[_root_.fs2.Pure]($v, $e): _root_.com.mobimeo.gtfs.rules.Transformation[_root_.fs2.Pure]"
      case Transformation.SearchAndReplace(fld, re, repl) =>
        q"_root_.com.mobimeo.gtfs.rules.Transformation.SearchAndReplace[_root_.fs2.Pure]($fld, $re, $repl): _root_.com.mobimeo.gtfs.rules.Transformation[_root_.fs2.Pure]"
    }

    implicit lazy val action: Liftable[Action[Pure]] = Liftable[Action[Pure]] {
      case Action.Delete() =>
        q"_root_.com.mobimeo.gtfs.rules.Action.Delete[_root_.fs2.Pure]()"
      case Action.Log(msg, lvl) =>
        q"_root_.com.mobimeo.gtfs.rules.Action.Log[_root_.fs2.Pure]($msg, $lvl)"
      case Action.Transform(ts) =>
        q"_root_.com.mobimeo.gtfs.rules.Action.Transform[_root_.fs2.Pure]($ts)"
    }

    implicit lazy val rule: Liftable[Rule[Pure]] = Liftable[Rule[Pure]] { r =>
      q"_root_.com.mobimeo.gtfs.rules.Rule[_root_.fs2.Pure](${r.name}, ${r.matcher}, ${r.action})"
    }

    implicit lazy val ruleset: Liftable[RuleSet[Pure]] = Liftable[RuleSet[Pure]] { r =>
      q"_root_.com.mobimeo.gtfs.rules.RuleSet[_root_.fs2.Pure](${r.file}, ${r.rules}, ${r.additions})"
    }

  }

  private object RuleSetLiteral extends Literally[RuleSet[Pure]] {
    def validate(ctx: Context)(s: String): Either[String, ctx.Expr[RuleSet[Pure]]] = {
      import ctx.universe._
      val liftables = new LiftableImpls {
        val c: ctx.type = ctx
      }
      import liftables._
      RuleParser.ruleset
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(rs => ctx.Expr(q"$rs"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[RuleSet[Pure]] = apply(c)(args: _*)
  }

  private object RuleLiteral extends Literally[Rule[Pure]] {
    def validate(ctx: Context)(s: String): Either[String, ctx.Expr[Rule[Pure]]] = {
      import ctx.universe._
      val liftables = new LiftableImpls {
        val c: ctx.type = ctx
      }
      import liftables._

      RuleParser.rule
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(r => ctx.Expr(q"$r"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Rule[Pure]] = apply(c)(args: _*)
  }

  private object MatcherLiteral extends Literally[Matcher] {
    def validate(ctx: Context)(s: String): Either[String, ctx.Expr[Matcher]] = {
      import ctx.universe._
      val liftables = new LiftableImpls {
        val c: ctx.type = ctx
      }
      import liftables._

      RuleParser.matcher
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(m => c.Expr(q"$m"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Matcher] = apply(c)(args: _*)
  }

  private object TransformationLiteral extends Literally[Transformation[Pure]] {
    def validate(ctx: Context)(s: String): Either[String, ctx.Expr[Transformation[Pure]]] = {
      import ctx.universe._
      val liftables = new LiftableImpls {
        val c: ctx.type = ctx
      }
      import liftables._

      RuleParser.transformation
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(m => c.Expr(q"$m"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Transformation[Pure]] = apply(c)(args: _*)
  }

  private object ActionLiteral extends Literally[Action[Pure]] {
    def validate(ctx: Context)(s: String): Either[String, ctx.Expr[Action[Pure]]] = {
      import ctx.universe._
      val liftables = new LiftableImpls {
        val c: ctx.type = ctx
      }
      import liftables._

      RuleParser.action
        .parseAll(s)
        .leftMap(error => new Prettyprint(s).prettyprint(error))
        .map(m => c.Expr(q"$m"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Action[Pure]] = apply(c)(args: _*)
  }

}
