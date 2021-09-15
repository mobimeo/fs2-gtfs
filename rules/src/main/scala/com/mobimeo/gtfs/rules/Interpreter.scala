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

import cats.MonadError
import cats.syntax.all._
import fs2.data.csv.CsvRow
import cats.ApplicativeError

/** An interpreter is used in the context of the engine to evaluate the various expressions from the [[Matcher]] s
  * and [[Transformation]] s.
  *
  * Expressions are pure, and cannot perform any side effects. Applicable functions transform a string value into
  * another one without access to any context.
  */
class Interpreter[F[_]](val functions: Map[String, DocumentedFunction[F]])(implicit F: MonadError[F, Throwable]) {

  /** Evalutes the value within the row context. If it cannot be resolved (e.g. field does not exist or value doesn't
    * exist in context), `None` is returned.
    */
  def eval(ctx: Ctx, row: CsvRow[String], value: Value): Option[String] =
    value match {
      case Value.Str(s) => Some(s)
      case Value.Field(name) =>
        eval(ctx, row, name).flatMap(row(_))
      case Value.Context(keys) =>
        keys.traverse(eval(ctx, row, _)).flatMap(ctx.get(_))

    }

  def eval(ctx: Ctx, row: CsvRow[String], e: Expr[F]): F[Option[String]] =
    e match {
      case Expr.NamedFunction(name, args) =>
        functions.get(name) match {
          case Some(fun) =>
            args
              .traverse(arg => eval(ctx, row, arg))
              .map(_.sequence)
              .flatMap(_.map(fun.f(_)).sequence)
          case None =>
            F.raiseError(new Exception(s"Unknown function $name"))
        }
      case Expr.AnonymousFunction(f, args) =>
        args
          .traverse(arg => eval(ctx, row, arg))
          .map(_.sequence)
          .flatMap(_.map(f(_)).sequence)
      case Expr.Val(v) => eval(ctx, row, v).pure[F]
    }

}

object Interpreter {

  def defaultFunctions[F[_]](implicit F: ApplicativeError[F, Throwable]): Map[String, DocumentedFunction[F]] =
    Map(
      "lowercase" -> DocumentedFunction.lift1(_.toLowerCase(), Some("Makes the argument lowercase")),
      "uppercase" -> DocumentedFunction.lift1(_.toUpperCase(), Some("Makes the argument uppercase")),
      "trim"      -> DocumentedFunction.lift1(_.trim(), Some("Trims the argument")),
      "concat"    -> DocumentedFunction(_.mkString("").pure[F], Arity.Unbounded, Some("Concatenates all arguments"))
    )

  def apply[F[_]](implicit F: MonadError[F, Throwable]): Interpreter[F] = new Interpreter(defaultFunctions)

  def apply[F[_]](functions: Map[String, DocumentedFunction[F]])(implicit
      F: MonadError[F, Throwable]
  ): Interpreter[F] = new Interpreter(functions)

}
