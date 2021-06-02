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

import cats.implicits._

import enumeratum._
import enumeratum.values.{IntEnum, IntEnumEntry}

import fs2.data.csv._
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto._

import java.time._
import java.{util => ju}
import scala.annotation.unused

trait CsvEnum[T <: EnumEntry] extends Enum[T] {

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.stringDecoder.emap(s =>
      withNameEither(s).leftMap(t => new DecoderError(s"Unknown enum value $s", None, t))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.stringEncoder.contramap(_.entryName)

}

trait CsvIntEnum[T <: IntEnumEntry] extends IntEnum[T] {

  implicit val cellDecoder: CellDecoder[T] =
    CellDecoder.intDecoder.emap(s =>
      withValueEither(s).leftMap(t => new DecoderError(s"Unknown enum value $s", None, t))
    )

  implicit val cellEncoder: CellEncoder[T] =
    CellEncoder.intEncoder.contramap(_.value)

}

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
}

sealed abstract class LocationType(val value: Int) extends IntEnumEntry
object LocationType extends IntEnum[LocationType] with CsvIntEnum[LocationType] {
  case object Stop         extends LocationType(0)
  case object Station      extends LocationType(1)
  case object Entrance     extends LocationType(2)
  case object GenericNode  extends LocationType(3)
  case object BoardingArea extends LocationType(4)

  def values: IndexedSeq[LocationType] = findValues
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
}

sealed abstract class SimpleRouteType(val value: Int) extends IntEnumEntry with EnumEntry
object SimpleRouteType extends IntEnum[SimpleRouteType] with CsvIntEnum[SimpleRouteType] {
  case object Tram       extends SimpleRouteType(0)
  case object Subway     extends SimpleRouteType(1)
  case object Rail       extends SimpleRouteType(2)
  case object Bus        extends SimpleRouteType(3)
  case object Ferry      extends SimpleRouteType(4)
  case object CableTram  extends SimpleRouteType(5)
  case object AerialLift extends SimpleRouteType(6)
  case object Funicular  extends SimpleRouteType(7)
  case object Trolleybus extends SimpleRouteType(11)
  case object Monorail   extends SimpleRouteType(12)

  def values: IndexedSeq[SimpleRouteType] = findValues
}

/** Extended route types.
  * See https://developers.google.com/transit/gtfs/reference/extended-route-types
  */
sealed abstract class ExtendedRouteType(val value: Int) extends IntEnumEntry with EnumEntry
object ExtendedRouteType extends IntEnum[ExtendedRouteType] with CsvIntEnum[ExtendedRouteType] {
  case object SomeRailwayService          extends ExtendedRouteType(100)
  case object HighSpeedRailService        extends ExtendedRouteType(101)
  case object LongDistanceTrains          extends ExtendedRouteType(102)
  case object InterRegionalRailService    extends ExtendedRouteType(103)
  case object CarTransportRailService     extends ExtendedRouteType(104)
  case object SleeperRailService          extends ExtendedRouteType(105)
  case object RegionalRailService         extends ExtendedRouteType(106)
  case object TouristRailwayService       extends ExtendedRouteType(107)
  case object RailShuttle                 extends ExtendedRouteType(108)
  case object SuburbanRailway             extends ExtendedRouteType(109)
  case object ReplacementRailService      extends ExtendedRouteType(110)
  case object SpecialRailService          extends ExtendedRouteType(111)
  case object LorryTransportRailService   extends ExtendedRouteType(112)
  case object AllRailServices             extends ExtendedRouteType(113)
  case object CrossCountryRailService     extends ExtendedRouteType(114)
  case object VehicleTransportRailService extends ExtendedRouteType(115)
  case object RackAndPinionRailway        extends ExtendedRouteType(116)
  case object AdditionalRailService       extends ExtendedRouteType(117)

  case object SomeCoachService          extends ExtendedRouteType(200)
  case object InternationalCoachService extends ExtendedRouteType(201)
  case object NationalCoachService      extends ExtendedRouteType(202)
  case object ShuttleCoachService       extends ExtendedRouteType(203)
  case object RegionalCoachService      extends ExtendedRouteType(204)
  case object SpecialCoachService       extends ExtendedRouteType(205)
  case object SightseeingCoachService   extends ExtendedRouteType(206)
  case object TouristCoachService       extends ExtendedRouteType(207)
  case object CommuterCoachService      extends ExtendedRouteType(208)
  case object AllCoachServices          extends ExtendedRouteType(209)

  case object SomeUrbanRailwayService extends ExtendedRouteType(400)
  case object MetroService            extends ExtendedRouteType(401)
  case object UndergroundService      extends ExtendedRouteType(402)
  case object UrbanRailwayService     extends ExtendedRouteType(403)
  case object AllUrbanRailwayServices extends ExtendedRouteType(404)
  case object Monorail                extends ExtendedRouteType(405)

  case object SomeBusService                   extends ExtendedRouteType(700)
  case object RegionalBusService               extends ExtendedRouteType(701)
  case object ExpressBusService                extends ExtendedRouteType(702)
  case object StoppingBusService               extends ExtendedRouteType(703)
  case object LocalBusService                  extends ExtendedRouteType(704)
  case object NightBusService                  extends ExtendedRouteType(705)
  case object PostBusService                   extends ExtendedRouteType(706)
  case object SpecialNeedsBus                  extends ExtendedRouteType(707)
  case object MobilityBusService               extends ExtendedRouteType(708)
  case object MobilityBusForRegisteredDisabled extends ExtendedRouteType(709)
  case object SightseeingBus                   extends ExtendedRouteType(710)
  case object ShuttleBus                       extends ExtendedRouteType(711)
  case object SchoolBus                        extends ExtendedRouteType(712)
  case object SchoolAndPublicServiceBus        extends ExtendedRouteType(713)
  case object RailReplacementBusService        extends ExtendedRouteType(714)
  case object DemandAndResponseBusService      extends ExtendedRouteType(715)
  case object AllBusServices                   extends ExtendedRouteType(716)

  case object SomeTrolleybusService extends ExtendedRouteType(800)

  case object SomeTramService        extends ExtendedRouteType(900)
  case object CityTramService        extends ExtendedRouteType(901)
  case object LocalTramService       extends ExtendedRouteType(902)
  case object RegionalTramService    extends ExtendedRouteType(903)
  case object SightseeingTramService extends ExtendedRouteType(904)
  case object ShuttleTramService     extends ExtendedRouteType(905)
  case object AllTranServices        extends ExtendedRouteType(906)

  case object SomeWaterTransportService extends ExtendedRouteType(1000)

  case object SomeAirTransportService extends ExtendedRouteType(1100)

  case object SomeFerryService extends ExtendedRouteType(1200)

  case object SomeAerialLiftService extends ExtendedRouteType(1300)

  case object SomeFunicularService extends ExtendedRouteType(1400)

  case object SomeTaxiService           extends ExtendedRouteType(1500)
  case object CommunalTaxiService       extends ExtendedRouteType(1501)
  case object WaterTaxiService          extends ExtendedRouteType(1502)
  case object RailTaxiService           extends ExtendedRouteType(1503)
  case object BikeTaxiService           extends ExtendedRouteType(1504)
  case object LicensedTaxiService       extends ExtendedRouteType(1505)
  case object PrivateHireServiceVehicle extends ExtendedRouteType(1506)
  case object AllTaxiServices           extends ExtendedRouteType(1507)

  case object MiscellaneousService extends ExtendedRouteType(1700)
  case object HorseDrawnCarriage   extends ExtendedRouteType(1702)

  def values: IndexedSeq[ExtendedRouteType] = findValues
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
  private val TimePattern = raw"(-?\d+):(\d{2}):(\d{2})".r
  implicit val cellDecoder: CellDecoder[SecondsSinceMidnight] =
    CellDecoder.stringDecoder.emap {
      case TimePattern(hours, minutes, seconds) =>
        Right(new SecondsSinceMidnight(hours.toInt * 3600 + minutes.toInt * 60 + seconds.toInt))
      case s =>
        Left(new DecoderError(s"Invalid time '$s'"))
    }

  implicit val cellEncoder: CellEncoder[SecondsSinceMidnight] =
    CellEncoder.stringEncoder.contramap(seconds =>
      s"${seconds.seconds / 3600}:${(math.abs(seconds.seconds) % 3600) / 60}:${math.abs(seconds.seconds) % 60}"
    )
}

sealed abstract class PickupOrDropOffType(val value: Int) extends IntEnumEntry
object PickupOrDropOffType extends IntEnum[PickupOrDropOffType] with CsvIntEnum[PickupOrDropOffType] {
  case object RegularlyScheduled       extends PickupOrDropOffType(0)
  case object None                     extends PickupOrDropOffType(1)
  case object MustPhoneAgency          extends PickupOrDropOffType(2)
  case object MustCoordinateWithDriver extends PickupOrDropOffType(3)

  def values: IndexedSeq[PickupOrDropOffType] = findValues
}

sealed abstract class Timepoint(val value: Int) extends IntEnumEntry
object Timepoint extends IntEnum[Timepoint] with CsvIntEnum[Timepoint] {
  case object Approximate extends Timepoint(0)
  case object Exact       extends Timepoint(1)

  def values: IndexedSeq[Timepoint] = findValues
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

sealed abstract class Availability(val value: Int) extends IntEnumEntry
object Availability extends IntEnum[Availability] with CsvIntEnum[Availability] {
  case object Availabile  extends Availability(1)
  case object Unavailable extends Availability(0)

  def values: IndexedSeq[Availability] = findValues
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

sealed abstract class ExceptionType(val value: Int) extends IntEnumEntry
object ExceptionType extends IntEnum[ExceptionType] with CsvIntEnum[ExceptionType] {
  case object Added   extends ExceptionType(1)
  case object Removed extends ExceptionType(2)

  def values: IndexedSeq[ExceptionType] = findValues
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

sealed abstract class PaymentMethod(val value: Int) extends IntEnumEntry
object PaymentMethod extends IntEnum[PaymentMethod] with CsvIntEnum[PaymentMethod] {
  case object OnBoard        extends PaymentMethod(0)
  case object BeforeBoarding extends PaymentMethod(1)

  def values: IndexedSeq[PaymentMethod] = findValues
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

sealed abstract class ExactTimes(val value: Int) extends IntEnumEntry
object ExactTimes extends IntEnum[ExactTimes] with CsvIntEnum[ExactTimes] {
  case object FrequencyBased extends ExactTimes(0)
  case object ScheduleBased  extends ExactTimes(1)

  def values: IndexedSeq[ExactTimes] = findValues
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

sealed abstract class TransferType(val value: Int) extends IntEnumEntry
object TransferType extends IntEnum[TransferType] with CsvIntEnum[TransferType] {
  case object RecommendedTransfer         extends TransferType(0)
  case object TimedTransfer               extends TransferType(1)
  case object MinimumTimeRequiredTransfer extends TransferType(2)
  case object ImpossibleTransfer          extends TransferType(3)

  def values: IndexedSeq[TransferType] = findValues
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

sealed abstract class PathwayMode(val value: Int) extends IntEnumEntry
object PathwayMode extends IntEnum[PathwayMode] with CsvIntEnum[PathwayMode] {
  case object Walkway        extends PathwayMode(1)
  case object Stairs         extends PathwayMode(2)
  case object MovingSidewalk extends PathwayMode(3)
  case object Escalator      extends PathwayMode(4)
  case object Elevator       extends PathwayMode(5)
  case object FareGate       extends PathwayMode(6)
  case object ExitGate       extends PathwayMode(7)

  def values: IndexedSeq[PathwayMode] = findValues
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

sealed trait TableName extends EnumEntry with EnumEntry.Snakecase
object TableName extends Enum[TableName] with CsvEnum[TableName] {
  case object Agency       extends TableName
  case object Stops        extends TableName
  case object Routes       extends TableName
  case object Trips        extends TableName
  case object StopTimes    extends TableName
  case object FeedInfo     extends TableName
  case object Pathways     extends TableName
  case object Levels       extends TableName
  case object Attributions extends TableName

  def values: IndexedSeq[TableName] = findValues
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
