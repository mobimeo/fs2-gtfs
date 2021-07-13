package com.mobimeo.gtfs.model

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class PaymentMethod(val value: Int) extends IntEnumEntry
object PaymentMethod extends IntEnum[PaymentMethod] with CsvIntEnum[PaymentMethod] {
  case object OnBoard        extends PaymentMethod(0)
  case object BeforeBoarding extends PaymentMethod(1)

  val values = findValues
}
