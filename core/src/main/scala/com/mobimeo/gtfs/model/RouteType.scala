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

/** Extended route types.
  *
  * See https://developers.google.com/transit/gtfs/reference/extended-route-types
  */
enum RouteType(val value: Int) extends IntEnumEntry {
  case Tram       extends RouteType(0)
  case Subway     extends RouteType(1)
  case Rail       extends RouteType(2)
  case Bus        extends RouteType(3)
  case Ferry      extends RouteType(4)
  case CableTram  extends RouteType(5)
  case AerialLift extends RouteType(6)
  case Funicular  extends RouteType(7)
  case Trolleybus extends RouteType(11)
  case MonorailSimple   extends RouteType(12)

  case SomeRailwayService          extends RouteType(100)
  case HighSpeedRailService        extends RouteType(101)
  case LongDistanceTrains          extends RouteType(102)
  case InterRegionalRailService    extends RouteType(103)
  case CarTransportRailService     extends RouteType(104)
  case SleeperRailService          extends RouteType(105)
  case RegionalRailService         extends RouteType(106)
  case TouristRailwayService       extends RouteType(107)
  case RailShuttle                 extends RouteType(108)
  case SuburbanRailway             extends RouteType(109)
  case ReplacementRailService      extends RouteType(110)
  case SpecialRailService          extends RouteType(111)
  case LorryTransportRailService   extends RouteType(112)
  case AllRailServices             extends RouteType(113)
  case CrossCountryRailService     extends RouteType(114)
  case VehicleTransportRailService extends RouteType(115)
  case RackAndPinionRailway        extends RouteType(116)
  case AdditionalRailService       extends RouteType(117)

  case SomeCoachService          extends RouteType(200)
  case InternationalCoachService extends RouteType(201)
  case NationalCoachService      extends RouteType(202)
  case ShuttleCoachService       extends RouteType(203)
  case RegionalCoachService      extends RouteType(204)
  case SpecialCoachService       extends RouteType(205)
  case SightseeingCoachService   extends RouteType(206)
  case TouristCoachService       extends RouteType(207)
  case CommuterCoachService      extends RouteType(208)
  case AllCoachServices          extends RouteType(209)

  case SomeUrbanRailwayService extends RouteType(400)
  case MetroService            extends RouteType(401)
  case UndergroundService      extends RouteType(402)
  case UrbanRailwayService     extends RouteType(403)
  case AllUrbanRailwayServices extends RouteType(404)
  case Monorail                extends RouteType(405)

  case SomeBusService                   extends RouteType(700)
  case RegionalBusService               extends RouteType(701)
  case ExpressBusService                extends RouteType(702)
  case StoppingBusService               extends RouteType(703)
  case LocalBusService                  extends RouteType(704)
  case NightBusService                  extends RouteType(705)
  case PostBusService                   extends RouteType(706)
  case SpecialNeedsBus                  extends RouteType(707)
  case MobilityBusService               extends RouteType(708)
  case MobilityBusForRegisteredDisabled extends RouteType(709)
  case SightseeingBus                   extends RouteType(710)
  case ShuttleBus                       extends RouteType(711)
  case SchoolBus                        extends RouteType(712)
  case SchoolAndPublicServiceBus        extends RouteType(713)
  case RailReplacementBusService        extends RouteType(714)
  case DemandAndResponseBusService      extends RouteType(715)
  case AllBusServices                   extends RouteType(716)

  case SomeTrolleybusService extends RouteType(800)

  case SomeTramService        extends RouteType(900)
  case CityTramService        extends RouteType(901)
  case LocalTramService       extends RouteType(902)
  case RegionalTramService    extends RouteType(903)
  case SightseeingTramService extends RouteType(904)
  case ShuttleTramService     extends RouteType(905)
  case AllTranServices        extends RouteType(906)

  case SomeWaterTransportService extends RouteType(1000)

  case SomeAirTransportService extends RouteType(1100)

  case SomeFerryService extends RouteType(1200)

  case SomeAerialLiftService extends RouteType(1300)

  case SomeFunicularService extends RouteType(1400)

  case SomeTaxiService           extends RouteType(1500)
  case CommunalTaxiService       extends RouteType(1501)
  case WaterTaxiService          extends RouteType(1502)
  case RailTaxiService           extends RouteType(1503)
  case BikeTaxiService           extends RouteType(1504)
  case LicensedTaxiService       extends RouteType(1505)
  case PrivateHireServiceVehicle extends RouteType(1506)
  case AllTaxiServices           extends RouteType(1507)

  case MiscellaneousService extends RouteType(1700)
  case HorseDrawnCarriage   extends RouteType(1702)
}

object RouteType extends CsvIntEnum[RouteType]
