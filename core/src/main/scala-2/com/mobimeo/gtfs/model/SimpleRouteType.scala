package com.mobimeo.gtfs.model

import enumeratum.EnumEntry
import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class SimpleRouteType(val value: Int) extends IntEnumEntry with EnumEntry
object SimpleRouteType extends IntEnum[SimpleRouteType] with CsvIntEnum[SimpleRouteType] {
  case object Tram       extends SimpleRouteType(0)
  case object Subway     extends SimpleRouteType(1)
  case object Rail       extends SimpleRouteType(2)
  case object Bus        extends SimpleRouteType(3)
  case object Ferry      extends SimpleRouteType(4)
  case object CableTram  extends SimpleRouteType(5)
  case object AerialLift extends SimpleRouteType(6)
  case object Funicular  extends SimpleRouteType(7)
  case object Trolleybus extends SimpleRouteType(11)
  case object Monorail   extends SimpleRouteType(12)

  val values = findValues
}
