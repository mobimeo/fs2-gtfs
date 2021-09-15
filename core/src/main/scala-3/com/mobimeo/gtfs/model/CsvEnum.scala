package com.mobimeo.gtfs.model

import cats.syntax.all._
import fs2.data.csv.{CellDecoder, CellEncoder, DecoderError}

import scala.util.Try

trait EnumEntry {
  def entryName: String
}

trait CsvEnum[T <: EnumEntry] {

  def values: Array[T]

  lazy val byEntryName: Map[String, T] = values.toList.groupMapReduce(_.entryName)(identity)((e, _) => e)

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.stringDecoder.emap(s => byEntryName.get(s).toRight(new DecoderError(s"Unknown enum value $s")))

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.stringEncoder.contramap(_.entryName)

}

trait IntEnumEntry {
  def value: Int
}

trait CsvIntEnum[T <: IntEnumEntry] {

  def values: Array[T]

  // Build an array for quick int->value lookup using the fact GTFS has small known key spaces
  // Performance optimization for decoding a lot of T instances as we don't have to search the value space each time
  private lazy val valueLookup: Array[Option[T]] = {
    val grouped = values.toList.groupBy(_.value)
    Array.tabulate(grouped.keySet.max + 1)(grouped.get(_).flatMap(_.headOption))
  }

  def valueOf(n: Int): Option[T] = valueLookup(n)

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.intDecoder.emap(n => valueOf(n).toRight(new DecoderError(s"Unknown enum value $n")))

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.intEncoder.contramap(_.value)

}

trait OrdinalBasedCsvIntEnum[T <: IntEnumEntry] extends CsvIntEnum[T] {
  def fromOrdinal(n: Int): T

  override def valueOf(n: Int): Option[T] = Try(fromOrdinal(n)).toOption
}
