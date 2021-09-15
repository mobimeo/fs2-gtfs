package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class ExactTimes(val value: Int) extends IntEnumEntry
object ExactTimes extends IntEnum[ExactTimes] with CsvIntEnum[ExactTimes] {
  case object FrequencyBased extends ExactTimes(0)
  case object ScheduleBased  extends ExactTimes(1)

  val values = findValues
}
