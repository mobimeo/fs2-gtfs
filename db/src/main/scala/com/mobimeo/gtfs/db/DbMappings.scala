package com.mobimeo.gtfs.db

import doobie._
import com.mobimeo.gtfs.model._
import java.time.{ZoneId, LocalDate}
import java.util.Currency
import java.util.Locale

trait DbMappings {
  implicit val zoneIdGet: Get[ZoneId]                           = Get[String].tmap(ZoneId.of)
  implicit val zoneIdPut: Put[ZoneId]                           = Put[String].tcontramap(_.toString)
  implicit val localDateGet: Get[LocalDate]                     = Get[String].tmap(LocalDate.parse)
  implicit val localDatePut: Put[LocalDate]                     = Put[String].tcontramap(_.toString)
  implicit val locationTypePut: Put[LocationType]               = Put[Int].tcontramap(_.value)
  implicit val locationTypeGet: Get[LocationType]               = Get[Int].tmap(LocationType.withValue)
  implicit val pickupOrDropOffTypeGet: Get[PickupOrDropOffType] = Get[Int].tmap(PickupOrDropOffType.withValue)
  implicit val pickupOrDropOffTypePut: Put[PickupOrDropOffType] = Put[Int].tcontramap(_.value)
  implicit val timepointGet: Get[Timepoint]                     = Get[Int].tmap(Timepoint.withValue)
  implicit val timepointPut: Put[Timepoint]                     = Put[Int].tcontramap(_.value)
  implicit val transferTypeGet: Get[TransferType]               = Get[Int].tmap(TransferType.withValue)
  implicit val transferTypePut: Put[TransferType]               = Put[Int].tcontramap(_.value)
  implicit val availabilityGet: Get[Availability]               = Get[Int].tmap(Availability.withValue)
  implicit val availabilityPut: Put[Availability]               = Put[Int].tcontramap(_.value)
  implicit val exceptionTypeGet: Get[ExceptionType]             = Get[Int].tmap(ExceptionType.withValue)
  implicit val exceptionTypePut: Put[ExceptionType]             = Put[Int].tcontramap(_.value)
  implicit val currencyGet: Get[Currency]                       = Get[String].tmap(Currency.getInstance)
  implicit val currencyPut: Put[Currency]                       = Put[String].tcontramap(_.toString)
  implicit val paymentMethodGet: Get[PaymentMethod]             = Get[Int].tmap(PaymentMethod.withValue)
  implicit val paymentMethodPut: Put[PaymentMethod]             = Put[Int].tcontramap(_.value)
  implicit val exactTimesGet: Get[ExactTimes]                   = Get[Int].tmap(ExactTimes.withValue)
  implicit val exactTimesPut: Put[ExactTimes]                   = Put[Int].tcontramap(_.value)
  implicit val pathwayModeTimesGet: Get[PathwayMode]            = Get[Int].tmap(PathwayMode.withValue)
  implicit val pathwayModeTimesPut: Put[PathwayMode]            = Put[Int].tcontramap(_.value)
  implicit val localeGet: Get[Locale] = Get[String].tmap { s => 
    val Array(language, country) = s.split(":") 
    new Locale(language, country)
  }
  implicit val localePut: Put[Locale] = Put[String].tcontramap(locale => s"${locale.getLanguage()}:${locale.getCountry()}")
  implicit val tableNameGet: Get[TableName] = Get[String].tmap(TableName.withName)
  implicit val tableNamePut: Put[TableName] = Put[String].tcontramap(_.entryName)
}
