package com.mobimeo.gtfs.model

enum PathwayMode(val value: Int) extends IntEnumEntry {
  case Walkway        extends PathwayMode(1)
  case Stairs         extends PathwayMode(2)
  case MovingSidewalk extends PathwayMode(3)
  case Escalator      extends PathwayMode(4)
  case Elevator       extends PathwayMode(5)
  case FareGate       extends PathwayMode(6)
  case ExitGate       extends PathwayMode(7)
}

object PathwayMode extends CsvIntEnum[PathwayMode]
