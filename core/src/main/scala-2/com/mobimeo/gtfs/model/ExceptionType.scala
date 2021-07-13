package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class ExceptionType(val value: Int) extends IntEnumEntry
object ExceptionType extends IntEnum[ExceptionType] with CsvIntEnum[ExceptionType] {
  case object Added   extends ExceptionType(1)
  case object Removed extends ExceptionType(2)

  val values = findValues
}
