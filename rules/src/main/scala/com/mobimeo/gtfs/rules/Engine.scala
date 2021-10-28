/*
 * Copyright 2021 Mobimeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import cats.effect.Sync
import cats.syntax.all._
import com.mobimeo.gtfs.file.GtfsFile
import fs2._
import fs2.data.csv._
import org.typelevel.log4cats.StructuredLogger
import cats.data.OptionT

/** An engine is used to apply set of processing rules to GTFS files. It provides a common environment composed of
  * functions that can be used in the rules.
  *
  * An engine can be reused for several set of rules and GTFS files using the same set of environment.
  */
class Engine[F[_]](interpreter: Interpreter[F])(implicit
    F: Sync[F],
    logger: StructuredLogger[F]
) {

  /** Applies the rule sets to the `from` file, writing results in `to`.
    *
    * Rule sets are fused to avoid going over the same file several times when several rule sets apply to the same file.
    * The order in which the files are processed is not specified (and can even be done in parallel) but the different
    * rule sets operating on a single file are ensured to be run in the same order as provided.
    */
  def process[G[x] <: F[x]](rulesets: List[RuleSet[G]], from: GtfsFile[F], to: GtfsFile[F], ctx: Ctx = Ctx.empty): F[Unit] = {
    // fusing rule sets allows to only go through the entities once
    // even if several rulesets are defined for a given file
    // this is possible because each ruleset consider the rows in isolation
    // so as long as the order is kept the semantic stays the same
    val fusedRulesets = rulesets.groupMap(_.file)(rs => (rs.rules, rs.additions)).toList
    fusedRulesets.traverse_ { case (file, rulessadditions) =>
      val (ruless, additionss) = rulessadditions.unzip
      logger.info(s"Apply rules to $file for feed from ${from.file} to ${to.file}") >>
        from.read
          .rawFile(file)
          .pull
          .peek1
          .flatMap {
            case Some((CsvRow(_, headers), s)) =>
              (s.through(processRules(ruless, ctx)) ++ Stream
                .emits(additionss.flatten)
                .evalMap(CsvRow(_, headers).liftTo[F])).pull.echo
            case None => Pull.done
          }
          .stream
          .through(to.write.rawFile(file))
          .compile
          .drain
    }
  }

  private def processRules(ruless: List[List[Rule[F]]], ctx: Ctx): Pipe[F, CsvRow[String], CsvRow[String]] =
    _.evalMap(processRow(ruless, ctx, _)).unNone

  private def processRow(ruless: List[List[Rule[F]]], ctx: Ctx, row: CsvRow[String]): F[Option[CsvRow[String]]] =
    F.tailRecM((row, ruless)) {
      case (row, rules :: ruless) =>
        rules.find(rule => matches(ctx, row, rule.matcher)) match {
          case Some(rule) =>
            logger.debug(row.toMap)(s"Apply rule ${rule.name}") >> apply(ctx, row, rule.action)
              .map {
                case Some(row) =>
                  (row, ruless).asLeft
                case None =>
                  // row was deleted, stop here
                  none[CsvRow[String]].asRight
              }
              .handleErrorWith { t =>
                logger
                  .error(row.toMap, t)(s"An exception occured while applying rule ${rule.name}, row left unchanged")
                  .as(Left((row, ruless)))
              }
          case None =>
            (row, ruless).asLeft.pure[F]
        }
      case (row, Nil) =>
        row.some.asRight.pure[F]
    }

  def matches(ctx: Ctx, row: CsvRow[String], matcher: Matcher): Boolean =
    matcher match {
      case Matcher.Any => true
      case Matcher.Equals(left, right) =>
        (interpreter.eval(ctx, row, left), interpreter.eval(ctx, row, right)).mapN(_ == _).getOrElse(false)
      case Matcher.In(value, values) =>
        (interpreter
          .eval(ctx, row, value))
          .map(value => values.flatMap(interpreter.eval(ctx, row, _)).contains(value))
          .getOrElse(false)
      case Matcher.Matches(value, regex) =>
        interpreter.eval(ctx, row, value).map(_.matches(regex)).getOrElse(false)
      case Matcher.Exists(Value.Field(name)) =>
        interpreter.eval(ctx, row, name).map(row(_).isDefined).getOrElse(false)
      case Matcher.Exists(Value.Context(keys)) =>
        keys.traverse(interpreter.eval(ctx, row, _)).map(ctx.get(_).isDefined).getOrElse(false)
      case Matcher.And(left, right) => matches(ctx, row, left) && matches(ctx, row, right)
      case Matcher.Or(left, right)  => matches(ctx, row, left) || matches(ctx, row, right)
      case Matcher.Not(inner)       => !matches(ctx, row, inner)
    }

  private def apply(ctx: Ctx, row: CsvRow[String], action: Action[F]): F[Option[CsvRow[String]]] =
    action match {
      case Action.Delete() => none.pure[F]
      case Action.Log(msg, level) =>
        OptionT(interpreter.eval(ctx, row, msg)).semiflatMap(log(row, _, level)).as(row).value
      case Action.Transform(transformations) => applyAll(ctx, row, transformations)
    }

  private def applyAll(
      ctx: Ctx,
      row: CsvRow[String],
      transformations: NonEmptyList[Transformation[F]]
  ): F[Option[CsvRow[String]]] =
    transformations.foldLeftM(row)(apply(ctx, _, _)).map(_.some)

  private def log(row: CsvRow[String], msg: String, level: LogLevel): F[Unit] =
    level match {
      case LogLevel.Debug   => logger.debug(row.toMap)(msg)
      case LogLevel.Info    => logger.info(row.toMap)(msg)
      case LogLevel.Warning => logger.warn(row.toMap)(msg)
      case LogLevel.Error   => logger.error(row.toMap)(msg)
    }

  private def apply(ctx: Ctx, row: CsvRow[String], transformation: Transformation[F]): F[CsvRow[String]] =
    transformation match {
      case Transformation.Set(field, to) =>
        interpreter.eval(ctx, row, field) match {
          case Some(field) =>
            interpreter.eval(ctx, row, to).flatMap {
              case Some(to) =>
                logger.debug(row.toMap)(s"In field $field, replace value by '$to'").as(row.set(field, to))
              case None =>
                logger
                  .warn(row.toMap)(
                    show"No value found in context for set value 'row[$field] := $to', row left unchanged"
                  )
                  .as(row)
            }
          case None =>
            logger.warn(row.toMap)(s"Could not compute field value '$field', row left unchanged").as(row)
        }
      case Transformation.SearchAndReplace(field, regex, repl) =>
        interpreter.eval(ctx, row, field) match {
          case Some(field) =>
            logger
              .debug(row.toMap)(
                s"In field $field (value: '${row(field)
                  .getOrElse("")}'), replace all matching '$regex' with '$repl' -> ${row(field).getOrElse("").replaceAll(regex, repl)}"
              )
              .as(row.modify(field)(_.replaceAll(regex, repl)))
          case None =>
            logger
              .warn(row.toMap)(s"Could not compute field value '$field', row left unchanged")
              .as(row)
        }
    }

}

object Engine {

  def apply[F[_]: Sync: StructuredLogger]: Engine[F] =
    new Engine(Interpreter[F])

  def apply[F[_]: Sync: StructuredLogger](functions: Map[String, DocumentedFunction[F]]): Engine[F] =
    new Engine(Interpreter(functions))

  def apply[F[_]: Sync: StructuredLogger](interpreter: Interpreter[F]): Engine[F] =
    new Engine(interpreter)

}
