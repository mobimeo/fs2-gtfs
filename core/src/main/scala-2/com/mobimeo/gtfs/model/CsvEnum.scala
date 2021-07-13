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
