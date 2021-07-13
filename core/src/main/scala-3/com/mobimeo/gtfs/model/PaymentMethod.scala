package com.mobimeo.gtfs.model

enum PaymentMethod(val value: Int) extends IntEnumEntry {
  case OnBoard        extends PaymentMethod(0)
  case BeforeBoarding extends PaymentMethod(1)
}

object PaymentMethod extends OrdinalBasedCsvIntEnum[PaymentMethod]
