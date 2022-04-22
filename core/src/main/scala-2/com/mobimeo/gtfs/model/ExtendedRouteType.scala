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

import enumeratum.EnumEntry
import enumeratum.values.{IntEnum, IntEnumEntry}

/** Extended route types. See https://developers.google.com/transit/gtfs/reference/extended-route-types
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

  val values = findValues
}
