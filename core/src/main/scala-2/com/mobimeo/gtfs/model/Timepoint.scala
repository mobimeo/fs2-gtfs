package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class Timepoint(val value: Int) extends IntEnumEntry
object Timepoint extends IntEnum[Timepoint] with CsvIntEnum[Timepoint] {
  case object Approximate extends Timepoint(0)
  case object Exact       extends Timepoint(1)

  val values = findValues
}
