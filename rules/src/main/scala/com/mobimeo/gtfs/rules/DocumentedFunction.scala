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

import cats.{ApplicativeError, Show}
import cats.syntax.all._

case class DocumentedFunction[F[_]](f: List[String] => F[String], arity: Arity, doc: Option[String])
object DocumentedFunction {

  implicit def show[F[_]]: Show[DocumentedFunction[F]] = Show.show {
    case DocumentedFunction(_, arity, None) =>
      show"($arity) -> str"
    case DocumentedFunction(_, arity, Some(doc)) =>
      show"($arity) -> str\n\t$doc"
  }

  object markdown {

    implicit def show[F[_]]: Show[DocumentedFunction[F]] = Show.show {
      case DocumentedFunction(_, arity, None) =>
        show"`($arity) -> str`"
      case DocumentedFunction(_, arity, Some(doc)) =>
        show"`($arity) -> str`\n> $doc"
    }

  }

  def lift1[F[_]](f: String => String, doc: Option[String] = None)(implicit
      F: ApplicativeError[F, Throwable]
  ): DocumentedFunction[F] =
    DocumentedFunction(
      {
        case List(arg) => f(arg).pure[F]
        case args =>
          F.raiseError(
            new IllegalArgumentException(s"Unexepected number of arguments (expected 1 but go ${args.size})")
          )
      },
      Arity.Bounded(1),
      doc
    )

  def lift2[F[_]](
      f: (String, String) => String,
      doc: Option[String] = None
  )(implicit F: ApplicativeError[F, Throwable]): DocumentedFunction[F] = DocumentedFunction(
    {
      case List(arg1, arg2) => f(arg1, arg2).pure[F]
      case args =>
        F.raiseError(new IllegalArgumentException(s"Unexepected number of arguments (expected 2 but go ${args.size})"))
    },
    Arity.Bounded(2),
    doc
  )

  def liftF1[F[_]](f: String => F[String], doc: Option[String] = None)(implicit
      F: ApplicativeError[F, Throwable]
  ): DocumentedFunction[F] =
    DocumentedFunction(
      {
        case List(arg) => f(arg)
        case args =>
          F.raiseError(
            new IllegalArgumentException(s"Unexepected number of arguments (expected 1 but go ${args.size})")
          )
      },
      Arity.Bounded(1),
      doc
    )

  def liftF2[F[_]](
      f: (String, String) => F[String],
      doc: Option[String] = None
  )(implicit F: ApplicativeError[F, Throwable]): DocumentedFunction[F] = DocumentedFunction(
    {
      case List(arg1, arg2) => f(arg1, arg2)
      case args =>
        F.raiseError(new IllegalArgumentException(s"Unexepected number of arguments (expected 2 but go ${args.size})"))
    },
    Arity.Bounded(2),
    doc
  )

}

sealed trait Arity
object Arity {

  implicit val show: Show[Arity] = Show.show {
    case Unbounded  => "str*"
    case Bounded(n) => List.fill(n)("str").mkString(", ")
  }

  def apply(n: Int): Arity = Bounded(n)

  case object Unbounded          extends Arity
  case class Bounded(arity: Int) extends Arity
}
