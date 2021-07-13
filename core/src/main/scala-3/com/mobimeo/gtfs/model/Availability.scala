package com.mobimeo.gtfs.model

enum Availability(val value: Int) extends IntEnumEntry {
  case Unavailable extends Availability(0)
  case Availabile  extends Availability(1)
}

object Availability extends OrdinalBasedCsvIntEnum[Availability]
