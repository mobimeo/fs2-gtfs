package com.mobimeo.gtfs.model

enum ExceptionType(val value: Int) extends IntEnumEntry {
  case Added   extends ExceptionType(1)
  case Removed extends ExceptionType(2)
}

object ExceptionType extends CsvIntEnum[ExceptionType]
