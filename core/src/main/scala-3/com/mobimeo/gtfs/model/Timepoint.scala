package com.mobimeo.gtfs.model

enum Timepoint(val value: Int) extends IntEnumEntry {
  case Approximate extends Timepoint(0)
  case Exact       extends Timepoint(1)
}

object Timepoint extends OrdinalBasedCsvIntEnum[Timepoint]
