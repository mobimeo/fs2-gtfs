package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class PathwayMode(val value: Int) extends IntEnumEntry
object PathwayMode extends IntEnum[PathwayMode] with CsvIntEnum[PathwayMode] {
  case object Walkway        extends PathwayMode(1)
  case object Stairs         extends PathwayMode(2)
  case object MovingSidewalk extends PathwayMode(3)
  case object Escalator      extends PathwayMode(4)
  case object Elevator       extends PathwayMode(5)
  case object FareGate       extends PathwayMode(6)
  case object ExitGate       extends PathwayMode(7)

  val values = findValues
}
