package com.mobimeo.gtfs.model

enum ExactTimes(val value: Int) extends IntEnumEntry {
  case FrequencyBased extends ExactTimes(0)
  case ScheduleBased  extends ExactTimes(1)
}

object ExactTimes extends OrdinalBasedCsvIntEnum[ExactTimes]
