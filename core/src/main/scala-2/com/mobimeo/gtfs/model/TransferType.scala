package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class TransferType(val value: Int) extends IntEnumEntry
object TransferType extends IntEnum[TransferType] with CsvIntEnum[TransferType] {
  case object RecommendedTransfer         extends TransferType(0)
  case object TimedTransfer               extends TransferType(1)
  case object MinimumTimeRequiredTransfer extends TransferType(2)
  case object ImpossibleTransfer          extends TransferType(3)

  val values = findValues
}
