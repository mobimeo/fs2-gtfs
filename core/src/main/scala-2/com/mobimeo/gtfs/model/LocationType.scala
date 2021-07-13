package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class LocationType(val value: Int) extends IntEnumEntry
object LocationType extends IntEnum[LocationType] with CsvIntEnum[LocationType] {
  case object Stop         extends LocationType(0)
  case object Station      extends LocationType(1)
  case object Entrance     extends LocationType(2)
  case object GenericNode  extends LocationType(3)
  case object BoardingArea extends LocationType(4)

  val values = findValues
}
