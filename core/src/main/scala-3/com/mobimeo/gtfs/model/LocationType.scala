package com.mobimeo.gtfs.model

enum LocationType(val value: Int) extends IntEnumEntry {
  case Stop         extends LocationType(0)
  case Station      extends LocationType(1)
  case Entrance     extends LocationType(2)
  case GenericNode  extends LocationType(3)
  case BoardingArea extends LocationType(4)
}

object LocationType extends OrdinalBasedCsvIntEnum[LocationType]
