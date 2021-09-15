package com.mobimeo.gtfs.model

enum TransferType(val value: Int) extends IntEnumEntry {
  case RecommendedTransfer         extends TransferType(0)
  case TimedTransfer               extends TransferType(1)
  case MinimumTimeRequiredTransfer extends TransferType(2)
  case ImpossibleTransfer          extends TransferType(3)
}

object TransferType extends OrdinalBasedCsvIntEnum[TransferType]
