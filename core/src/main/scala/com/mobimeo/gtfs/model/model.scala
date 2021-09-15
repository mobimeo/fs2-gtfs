/*
 * Copyright 2021 Mobimeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobimeo.gtfs.model

import fs2.data.csv._
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto._

import java.time._
import java.{util => ju}
import scala.annotation.unused

case class Agency(
    @CsvName("agency_id")
    id: String,
    @CsvName("agency_name")
    name: String,
    @CsvName("agency_url")
    url: String,
    @CsvName("agency_timezone")
    timezone: ZoneId,
    @CsvName("agency_lang")
    language: Option[String],
    @CsvName("agency_phone")
    phone: Option[String],
    @CsvName("agency_fare_url")
    fareUrl: Option[String],
    @CsvName("agency_email")
    email: Option[String]
)

object Agency {
  implicit val csvRowDecoder: CsvRowDecoder[Agency, String] =
    deriveCsvRowDecoder[Agency]

  implicit val csvRowEncoder: CsvRowEncoder[Agency, String] =
    deriveCsvRowEncoder[Agency]
}

case class Stop(
    @CsvName("stop_id")
    id: String,
    @CsvName("stop_code")
    code: Option[String],
    @CsvName("stop_name")
    name: Option[String],
    @CsvName("stop_desc")
    desc: Option[String],
    @CsvName("stop_lat")
    lat: Option[Double],
    @CsvName("stop_lon")
    lon: Option[Double],
    @CsvName("zone_id")
    zoneId: Option[String],
    @CsvName("stop_url")
    url: Option[String],
    @CsvName("location_type")
    locationType: Option[LocationType],
    @CsvName("parent_station")
    parentStation: Option[String],
    @CsvName("stop_timezone")
    timezone: Option[ZoneId],
    @CsvName("wheelchair_boarding")
    wheelchairBoarding: Option[Int],
    @CsvName("level_id")
    levelId: Option[String],
    @CsvName("platform_code")
    platformCode: Option[String]
)

object Stop {
  implicit val csvRowDecoder: CsvRowDecoder[Stop, String] =
    deriveCsvRowDecoder[Stop]

  implicit val csvRowEncoder: CsvRowEncoder[Stop, String] =
    deriveCsvRowEncoder[Stop]
}

case class Route[RouteType](
    @CsvName("route_id")
    id: String,
    @CsvName("agency_id")
    agencyId: Option[String],
    @CsvName("route_short_name")
    shortName: Option[String],
    @CsvName("route_long_name")
    longName: Option[String],
    @CsvName("route_desc")
    desc: Option[String],
    @CsvName("route_type")
    tpe: RouteType,
    @CsvName("route_url")
    url: Option[String],
    @CsvName("route_color")
    color: Option[String],
    @CsvName("route_text_color")
    textColor: Option[String],
    @CsvName("route_sort_order")
    sortOrder: Option[Int]
)

object Route {
  implicit def csvRowDecoder[RouteType](implicit
      @unused decoder: CellDecoder[RouteType]
  ): CsvRowDecoder[Route[RouteType], String] =
    deriveCsvRowDecoder[Route[RouteType]]

  implicit def csvRowEncoder[RouteType](implicit
      @unused decoder: CellEncoder[RouteType]
  ): CsvRowEncoder[Route[RouteType], String] =
    deriveCsvRowEncoder[Route[RouteType]]
}

case class Trip(
    @CsvName("route_id")
    routeId: String,
    @CsvName("service_id")
    serviceId: String,
    @CsvName("trip_id")
    id: String,
    @CsvName("trip_headsign")
    headsign: Option[String],
    @CsvName("trip_short_name")
    shortName: Option[String],
    @CsvName("direction_id")
    directionId: Option[Int],
    @CsvName("block_id")
    blockId: Option[String],
    @CsvName("shape_id")
    shapeId: Option[String],
    @CsvName("wheelchair_accessible")
    wheelcharAccessible: Option[Int],
    @CsvName("bikes_allowed")
    bikesAllowed: Option[Int]
)

object Trip {
  implicit val csvRowDecoder: CsvRowDecoder[Trip, String] =
    deriveCsvRowDecoder[Trip]

  implicit val csvRowEncoder: CsvRowEncoder[Trip, String] =
    deriveCsvRowEncoder[Trip]
}

case class StopTime(
    @CsvName("trip_id")
    tripId: String,
    @CsvName("arrival_time")
    arrivalTime: SecondsSinceMidnight,
    @CsvName("departure_time")
    departureTime: SecondsSinceMidnight,
    @CsvName("stop_id")
    stopId: String,
    @CsvName("stop_sequence")
    stopSequence: Int,
    @CsvName("stop_headsign")
    stopHeadsign: Option[String],
    @CsvName("pickup_type")
    pickupType: Option[PickupOrDropOffType],
    @CsvName("drop_off_type")
    dropOffType: Option[PickupOrDropOffType],
    @CsvName("shape_dist_traveled")
    shapeDistTraveled: Option[Double],
    timepoint: Option[Timepoint]
)

object StopTime {
  implicit val decoder: CsvRowDecoder[StopTime, String] =
    deriveCsvRowDecoder[StopTime]

  implicit val encoder: CsvRowEncoder[StopTime, String] =
    deriveCsvRowEncoder[StopTime]
}

class SecondsSinceMidnight(val seconds: Int) extends AnyVal {
  def toLocalTime: LocalTime =
    LocalTime.ofSecondOfDay(seconds.toLong % (3600L * 24))
}

object SecondsSinceMidnight {
  private val TimePattern = raw"(-?\d+):(\d{1,2}):(\d{1,2})".r
  implicit val cellDecoder: CellDecoder[SecondsSinceMidnight] =
    CellDecoder.stringDecoder.emap {
      case TimePattern(hours, minutes, seconds) =>
        Right(new SecondsSinceMidnight(hours.toInt * 3600 + minutes.toInt * 60 + seconds.toInt))
      case s =>
        Left(new DecoderError(s"Invalid time '$s'"))
    }

  implicit val cellEncoder: CellEncoder[SecondsSinceMidnight] =
    CellEncoder.stringEncoder.contramap { seconds =>
      val hours = seconds.seconds / 3600
      val minutes = (math.abs(seconds.seconds) % 3600) / 60
      val secs = math.abs(seconds.seconds) % 60
      f"$hours%02d:$minutes%02d:$secs%02d"
    }
}

case class Calendar(
    @CsvName("service_id")
    serviceId: String,
    monday: Availability,
    tuesday: Availability,
    wednesday: Availability,
    thursday: Availability,
    friday: Availability,
    saturday: Availability,
    sunday: Availability,
    @CsvName("start_date")
    startDate: LocalDate,
    @CsvName("end_date")
    endDate: LocalDate
)
object Calendar {
  implicit val decoder: CsvRowDecoder[Calendar, String] =
    deriveCsvRowDecoder[Calendar]

  implicit val encoder: CsvRowEncoder[Calendar, String] =
    deriveCsvRowEncoder[Calendar]
}

case class CalendarDate(
    @CsvName("service_id")
    serviceId: String,
    date: LocalDate,
    @CsvName("exception_type")
    exceptionType: ExceptionType
)

object CalendarDate {
  implicit val decoder: CsvRowDecoder[CalendarDate, String] =
    deriveCsvRowDecoder[CalendarDate]

  implicit val encoder: CsvRowEncoder[CalendarDate, String] =
    deriveCsvRowEncoder[CalendarDate]
}

case class FareAttribute(
    @CsvName("fare_id")
    id: String,
    price: Double,
    @CsvName("currency_type")
    currency: ju.Currency,
    @CsvName("payment_method")
    paymentMethod: PaymentMethod,
    transfers: Option[Int],
    @CsvName("agency_id")
    agencyId: Option[String],
    @CsvName("transfer_duration")
    transferDuration: Option[Int]
)

object FareAttribute {
  implicit val decoder: CsvRowDecoder[FareAttribute, String] =
    deriveCsvRowDecoder[FareAttribute]

  implicit val encoder: CsvRowEncoder[FareAttribute, String] =
    deriveCsvRowEncoder[FareAttribute]
}

case class FareRules(
    @CsvName("fare_id")
    fareId: String,
    @CsvName("route_id")
    routeId: Option[String],
    @CsvName("origin_id")
    originId: Option[String],
    @CsvName("destination_id")
    destinationId: Option[String],
    @CsvName("contains_id")
    containsId: Option[String]
)

object FareRules {
  implicit val decoder: CsvRowDecoder[FareRules, String] =
    deriveCsvRowDecoder[FareRules]

  implicit val encoder: CsvRowEncoder[FareRules, String] =
    deriveCsvRowEncoder[FareRules]
}

case class Shape(
    @CsvName("shape_id")
    id: String,
    @CsvName("shape_pt_lat")
    lat: Double,
    @CsvName("shape_pt_lon")
    lon: Double,
    @CsvName("shape_pt_sequence")
    sequence: Int,
    @CsvName("shape_dist_traveled")
    distTraveled: Option[Double]
)

object Shape {
  implicit val decoder: CsvRowDecoder[Shape, String] =
    deriveCsvRowDecoder[Shape]

  implicit val encoder: CsvRowEncoder[Shape, String] =
    deriveCsvRowEncoder[Shape]
}

case class Frequency(
    @CsvName("trip_id")
    tripId: String,
    @CsvName("start_time")
    startTime: SecondsSinceMidnight,
    @CsvName("end_time")
    endTime: SecondsSinceMidnight,
    @CsvName("headway_secs")
    headwaySecs: Int,
    @CsvName("exact_times")
    exactTimes: Option[ExactTimes]
)

object Frequency {
  implicit val decoder: CsvRowDecoder[Frequency, String] =
    deriveCsvRowDecoder[Frequency]

  implicit val encoder: CsvRowEncoder[Frequency, String] =
    deriveCsvRowEncoder[Frequency]
}

case class Transfer(
    @CsvName("from_stop_id")
    fromStopId: String,
    @CsvName("to_stop_id")
    toStopId: String,
    @CsvName("transfer_type")
    transferType: TransferType,
    @CsvName("min_transfer_time")
    minTransferTime: Option[Int]
)

object Transfer {
  implicit val decoder: CsvRowDecoder[Transfer, String] =
    deriveCsvRowDecoder[Transfer]

  implicit val encoder: CsvRowEncoder[Transfer, String] =
    deriveCsvRowEncoder[Transfer]
}

case class Pathway(
    @CsvName("pathway_id")
    id: String,
    @CsvName("from_stop_id")
    fromStopId: String,
    @CsvName("to_stop_id")
    toStopId: String,
    @CsvName("pathway_mode")
    pathwayMode: PathwayMode,
    @CsvName("is_bidirectional")
    isBidirectional: Boolean,
    length: Option[Double],
    @CsvName("traversal_time")
    traversalTime: Option[Int],
    @CsvName("stair_count")
    stairCount: Option[Int],
    @CsvName("max_slope")
    maxSlope: Option[Double],
    @CsvName("min_width")
    minWidth: Option[Double],
    @CsvName("signposted_as")
    signpostedAs: Option[String],
    @CsvName("reversed_signposted_as")
    reverseSignpostedAs: Option[String]
)

object Pathway {
  implicit val decoder: CsvRowDecoder[Pathway, String] =
    deriveCsvRowDecoder[Pathway]

  implicit val encoder: CsvRowEncoder[Pathway, String] =
    deriveCsvRowEncoder[Pathway]
}

case class Level(
    @CsvName("level_id")
    id: String,
    @CsvName("level_index")
    index: Double,
    @CsvName("level_name")
    name: Option[String]
)

object Level {
  implicit val decoder: CsvRowDecoder[Level, String] =
    deriveCsvRowDecoder[Level]

  implicit val encoder: CsvRowEncoder[Level, String] =
    deriveCsvRowEncoder[Level]
}

case class FeedInfo(
    @CsvName("feed_publisher_name")
    publisherName: String,
    @CsvName("feed_publisher_url")
    publisherUrl: String,
    @CsvName("feed_lang")
    lang: ju.Locale,
    @CsvName("default_lang")
    defaultLang: Option[ju.Locale],
    @CsvName("feed_start_date")
    startDate: Option[LocalDate],
    @CsvName("feed_end_date")
    endDate: Option[LocalDate],
    @CsvName("feed_version")
    version: Option[String],
    @CsvName("feed_contact_email")
    contactEmail: Option[String],
    @CsvName("feed_contact_url")
    contactUrl: Option[String]
)

object FeedInfo {
  implicit val decoder: CsvRowDecoder[FeedInfo, String] =
    deriveCsvRowDecoder[FeedInfo]

  implicit val encoder: CsvRowEncoder[FeedInfo, String] =
    deriveCsvRowEncoder[FeedInfo]
}

case class Translation(
    @CsvName("table_name")
    tableName: TableName,
    @CsvName("field_name")
    fieldName: String,
    @CsvName("language")
    langage: ju.Locale,
    translation: String,
    @CsvName("record_id")
    recordId: Option[String],
    @CsvName("record_sub_id")
    recordSubId: Option[String],
    @CsvName("field_value")
    fieldValue: Option[String]
)

object Translation {
  implicit val decoder: CsvRowDecoder[Translation, String] =
    deriveCsvRowDecoder[Translation]

  implicit val encoder: CsvRowEncoder[Translation, String] =
    deriveCsvRowEncoder[Translation]
}

case class Attribution(
    @CsvName("attribution_id")
    id: Option[String],
    @CsvName("agency_id")
    agencyId: Option[String],
    @CsvName("route_id")
    routeId: Option[String],
    @CsvName("trip_id")
    tripId: Option[String],
    @CsvName("organization_name")
    organizationName: String,
    @CsvName("is_producer")
    isProducer: Option[Boolean],
    @CsvName("is_operator")
    isOperator: Option[Boolean],
    @CsvName("is_authority")
    isAuthority: Option[Boolean],
    @CsvName("attribution_url")
    url: Option[String],
    @CsvName("attribution_email")
    email: Option[String],
    @CsvName("attribution_phone")
    phone: Option[String]
)

object Attribution {
  implicit val decoder: CsvRowDecoder[Attribution, String] =
    deriveCsvRowDecoder[Attribution]

  implicit val encoder: CsvRowEncoder[Attribution, String] =
    deriveCsvRowEncoder[Attribution]
}
