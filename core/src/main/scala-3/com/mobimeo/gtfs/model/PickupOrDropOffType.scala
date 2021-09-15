package com.mobimeo.gtfs.model

enum PickupOrDropOffType(val value: Int) extends IntEnumEntry {
  case RegularlyScheduled       extends PickupOrDropOffType(0)
  case None                     extends PickupOrDropOffType(1)
  case MustPhoneAgency          extends PickupOrDropOffType(2)
  case MustCoordinateWithDriver extends PickupOrDropOffType(3)
}

object PickupOrDropOffType extends OrdinalBasedCsvIntEnum[PickupOrDropOffType]
