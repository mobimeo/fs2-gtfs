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

package com.mobimeo.gtfs.model

import cats.syntax.all._
import enumeratum._
import enumeratum.values._
import fs2.data.csv.{CellDecoder, CellEncoder, DecoderError}

trait CsvEnum[T <: EnumEntry] extends Enum[T] {

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.stringDecoder.emap(s =>
      withNameEither(s).leftMap(t => new DecoderError(s"Unknown enum value $s", None, t))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.stringEncoder.contramap(_.entryName)

}

trait CsvIntEnum[T <: IntEnumEntry] extends IntEnum[T] {

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.intDecoder.emap(s =>
      withValueEither(s).leftMap(t => new DecoderError(s"Unknown enum value $s", None, t))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.intEncoder.contramap(_.value)

}
