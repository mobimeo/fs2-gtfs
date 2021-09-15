package com.mobimeo.gtfs.model

enum SimpleRouteType(val value: Int) extends IntEnumEntry {
  case Tram       extends SimpleRouteType(0)
  case Subway     extends SimpleRouteType(1)
  case Rail       extends SimpleRouteType(2)
  case Bus        extends SimpleRouteType(3)
  case Ferry      extends SimpleRouteType(4)
  case CableTram  extends SimpleRouteType(5)
  case AerialLift extends SimpleRouteType(6)
  case Funicular  extends SimpleRouteType(7)
  case Trolleybus extends SimpleRouteType(11)
  case Monorail   extends SimpleRouteType(12)
}

object SimpleRouteType extends CsvIntEnum[SimpleRouteType]
