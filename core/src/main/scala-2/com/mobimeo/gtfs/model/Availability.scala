package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class Availability(val value: Int) extends IntEnumEntry
object Availability extends IntEnum[Availability] with CsvIntEnum[Availability] {
  case object Availabile  extends Availability(1)
  case object Unavailable extends Availability(0)

  val values = findValues
}
