package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class PickupOrDropOffType(val value: Int) extends IntEnumEntry
object PickupOrDropOffType extends IntEnum[PickupOrDropOffType] with CsvIntEnum[PickupOrDropOffType] {
  case object RegularlyScheduled       extends PickupOrDropOffType(0)
  case object None                     extends PickupOrDropOffType(1)
  case object MustPhoneAgency          extends PickupOrDropOffType(2)
  case object MustCoordinateWithDriver extends PickupOrDropOffType(3)

  val values = findValues
}
