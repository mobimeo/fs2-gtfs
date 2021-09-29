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
  * See https://developers.google.com/transit/gtfs/reference/extended-route-types
  */
enum ExtendedRouteType(val value: Int) extends IntEnumEntry {
  case SomeRailwayService          extends ExtendedRouteType(100)
  case HighSpeedRailService        extends ExtendedRouteType(101)
  case LongDistanceTrains          extends ExtendedRouteType(102)
  case InterRegionalRailService    extends ExtendedRouteType(103)
  case CarTransportRailService     extends ExtendedRouteType(104)
  case SleeperRailService          extends ExtendedRouteType(105)
  case RegionalRailService         extends ExtendedRouteType(106)
  case TouristRailwayService       extends ExtendedRouteType(107)
  case RailShuttle                 extends ExtendedRouteType(108)
  case SuburbanRailway             extends ExtendedRouteType(109)
  case ReplacementRailService      extends ExtendedRouteType(110)
  case SpecialRailService          extends ExtendedRouteType(111)
  case LorryTransportRailService   extends ExtendedRouteType(112)
  case AllRailServices             extends ExtendedRouteType(113)
  case CrossCountryRailService     extends ExtendedRouteType(114)
  case VehicleTransportRailService extends ExtendedRouteType(115)
  case RackAndPinionRailway        extends ExtendedRouteType(116)
  case AdditionalRailService       extends ExtendedRouteType(117)

  case SomeCoachService          extends ExtendedRouteType(200)
  case InternationalCoachService extends ExtendedRouteType(201)
  case NationalCoachService      extends ExtendedRouteType(202)
  case ShuttleCoachService       extends ExtendedRouteType(203)
  case RegionalCoachService      extends ExtendedRouteType(204)
  case SpecialCoachService       extends ExtendedRouteType(205)
  case SightseeingCoachService   extends ExtendedRouteType(206)
  case TouristCoachService       extends ExtendedRouteType(207)
  case CommuterCoachService      extends ExtendedRouteType(208)
  case AllCoachServices          extends ExtendedRouteType(209)

  case SomeUrbanRailwayService extends ExtendedRouteType(400)
  case MetroService            extends ExtendedRouteType(401)
  case UndergroundService      extends ExtendedRouteType(402)
  case UrbanRailwayService     extends ExtendedRouteType(403)
  case AllUrbanRailwayServices extends ExtendedRouteType(404)
  case Monorail                extends ExtendedRouteType(405)

  case SomeBusService                   extends ExtendedRouteType(700)
  case RegionalBusService               extends ExtendedRouteType(701)
  case ExpressBusService                extends ExtendedRouteType(702)
  case StoppingBusService               extends ExtendedRouteType(703)
  case LocalBusService                  extends ExtendedRouteType(704)
  case NightBusService                  extends ExtendedRouteType(705)
  case PostBusService                   extends ExtendedRouteType(706)
  case SpecialNeedsBus                  extends ExtendedRouteType(707)
  case MobilityBusService               extends ExtendedRouteType(708)
  case MobilityBusForRegisteredDisabled extends ExtendedRouteType(709)
  case SightseeingBus                   extends ExtendedRouteType(710)
  case ShuttleBus                       extends ExtendedRouteType(711)
  case SchoolBus                        extends ExtendedRouteType(712)
  case SchoolAndPublicServiceBus        extends ExtendedRouteType(713)
  case RailReplacementBusService        extends ExtendedRouteType(714)
  case DemandAndResponseBusService      extends ExtendedRouteType(715)
  case AllBusServices                   extends ExtendedRouteType(716)

  case SomeTrolleybusService extends ExtendedRouteType(800)

  case SomeTramService        extends ExtendedRouteType(900)
  case CityTramService        extends ExtendedRouteType(901)
  case LocalTramService       extends ExtendedRouteType(902)
  case RegionalTramService    extends ExtendedRouteType(903)
  case SightseeingTramService extends ExtendedRouteType(904)
  case ShuttleTramService     extends ExtendedRouteType(905)
  case AllTranServices        extends ExtendedRouteType(906)

  case SomeWaterTransportService extends ExtendedRouteType(1000)

  case SomeAirTransportService extends ExtendedRouteType(1100)

  case SomeFerryService extends ExtendedRouteType(1200)

  case SomeAerialLiftService extends ExtendedRouteType(1300)

  case SomeFunicularService extends ExtendedRouteType(1400)

  case SomeTaxiService           extends ExtendedRouteType(1500)
  case CommunalTaxiService       extends ExtendedRouteType(1501)
  case WaterTaxiService          extends ExtendedRouteType(1502)
  case RailTaxiService           extends ExtendedRouteType(1503)
  case BikeTaxiService           extends ExtendedRouteType(1504)
  case LicensedTaxiService       extends ExtendedRouteType(1505)
  case PrivateHireServiceVehicle extends ExtendedRouteType(1506)
  case AllTaxiServices           extends ExtendedRouteType(1507)

  case MiscellaneousService extends ExtendedRouteType(1700)
  case HorseDrawnCarriage   extends ExtendedRouteType(1702)
}

object ExtendedRouteType extends CsvIntEnum[ExtendedRouteType]

