package com.mobimeo.gtfs.model

import cats.syntax.all._
import fs2.data.csv.{CellDecoder, CellEncoder, DecoderError}

import scala.util.Try

trait EnumEntry {
  def entryName: String
}

trait CsvEnum[T <: EnumEntry] {

  def values: Array[T]

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.stringDecoder.emap(s =>
      values.find(_.entryName == s).toRight(new DecoderError(s"Unknown enum value $s"))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.stringEncoder.contramap(_.entryName)

}

trait IntEnumEntry {
  def value: Int
}

trait CsvIntEnum[T <: IntEnumEntry] {

  def values: Array[T]

  def valueOf(n: Int): Option[T] = values.find(_.value == n)

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.intDecoder.emap(n =>
      valueOf(n).toRight(new DecoderError(s"Unknown enum value $n"))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.intEncoder.contramap(_.value)

}

trait OrdinalBasedCsvIntEnum[T <: IntEnumEntry] extends CsvIntEnum[T] {
  def fromOrdinal(n: Int): T

  override def valueOf(n: Int): Option[T] = Try(fromOrdinal(n)).toOption
}
