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

import cats.effect._
import fs2.data.csv.CsvRow
import weaver._
import cats.data.NonEmptyList
import cats.syntax.all._
import Ctx.syntax._

object InterpreterSuite extends MutableIOSuite {

  type Res = (Interpreter[IO], CsvRow[String])

  def sharedResource: Resource[IO, Res] =
    Resource
      .eval(CsvRow[String](NonEmptyList.of("1", "2", "3"), NonEmptyList.of("a", "b", "c")).liftTo[IO])
      .map(Interpreter[IO] -> _)

  test("a row access value should be evaluated to the row field value") { case (interpreter, row) =>
    IO.pure(expect(interpreter.eval(Ctx.empty, row, Value.Field(Value.Str("b"))) == Some("2")))
  }

  test("a row access value should return non if the row doesn't have the field") { case (interpreter, row) =>
    IO.pure(expect(interpreter.eval(Ctx.empty, row, Value.Field(Value.Str("d"))) == None))
  }

  test("a row access value should return non if the field cannot be computed") { case (interpreter, row) =>
    IO.pure(
      expect(interpreter.eval(Ctx.empty, row, Value.Field(Value.Context(NonEmptyList.of(Value.Str("a"))))) == None)
    )
  }

  test("a context access value should be evaluated to the context value") { case (interpreter, row) =>
    IO.pure(
      expect(
        interpreter.eval(
          Ctx("a" := "1", "b" := Ctx("a" := "2")),
          row,
          Value.Context(NonEmptyList.of(Value.Str("b"), Value.Str("a")))
        ) == Some("2")
      )
    )
  }

  test("a context access value should be evaluated to None if it doesn't exist") { case (interpreter, row) =>
    val ctx = Ctx("a" := "1", "b" := Ctx("a" := "2"))
    IO.pure(
      expect(
        interpreter.eval(
          ctx,
          row,
          Value.Context(NonEmptyList.of(Value.Str("a"), Value.Str("b")))
        ) == None
      ) and expect(interpreter.eval(ctx, row, Value.Context(NonEmptyList.of(Value.Str("b")))) == None)
    )
  }

  test("a context access value should be evaluated to None if the key cannot be computed") { case (interpreter, row) =>
    val ctx = Ctx("a" := "1", "b" := Ctx("a" := "2"))
    IO.pure(
      expect(
        interpreter.eval(
          ctx,
          row,
          Value.Context(NonEmptyList.of(Value.Context(NonEmptyList.of(Value.Str("c"))), Value.Str("b")))
        ) == None
      )
    )
  }

}
