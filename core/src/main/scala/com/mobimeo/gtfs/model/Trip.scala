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

import fs2.data.csv.*
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto.*

/**
  * Trips for each route. A trip is a sequence of two or more stops that occur during a specific time period.
  *
  * @param routeId Identifies a route.
  * @param serviceId Identifies a set of dates when service is available for one or more routes.
  * @param id Identifies a trip.
  * @param headsign Text that appears on signage identifying the trip's destination to riders.
  *       Should be used to distinguish between different patterns of service on the same route.
  *
  *       If the headsign changes during a trip, values for trip_headsign may be overridden by defining values in stop_times.stop_headsign for specific stop_times along the trip.
  * @param shortName Public facing text used to identify the trip to riders, for instance, to identify train numbers for commuter rail trips.
  *       If riders do not commonly rely on trip names, trip_short_name should be empty.
  *       A trip_short_name value, if provided, should uniquely identify a trip within a service day; it should not be used for destination names or limited/express designations.
  * @param directionId Indicates the direction of travel for a trip.
  *       This field should not be used in routing; it provides a way to separate trips by direction when publishing time tables.
  *
  *       Valid options are:
  *       0 - Travel in one direction (e.g. outbound travel).
  *       1 - Travel in the opposite direction (e.g. inbound travel).
  *
  *       Example: The trip_headsign and direction_id fields may be used together to assign a name to travel in each direction for a set of trips.
  *       A trips.txt file could contain these records for use in time tables:
  *       trip_id,...,trip_headsign,direction_id
  *       1234,...,Airport,0
  *       1505,...,Downtown,1
  * @param blockId Identifies the block to which the trip belongs.
  *       A block consists of a single trip or many sequential trips made using the same vehicle, defined by shared service days and block_id.
  *       A block_id may have trips with different service days, making distinct blocks.
  *       See the example below.
  *       To provide in-seat transfers information, transfers of transfer_type 4 should be provided instead.
  * @param shapeId Identifies a geospatial shape describing the vehicle travel path for a trip.
  *
  *       Conditionally Required:
  *       - Required if the trip has a continuous pickup or drop-off behavior defined either in routes.txt or in stop_times.txt.
  *       - Optional otherwise.
  * @param wheelchairAccessible Indicates wheelchair accessibility.
  *
  *       Valid options are:
  *       0 or empty - No accessibility information for the trip.
  *       1 - Vehicle being used on this particular trip can accommodate at least one rider in a wheelchair.
  *       2 - No riders in wheelchairs can be accommodated on this trip.
  * @param bikesAllowed Indicates whether bikes are allowed.
  *
  *       Valid options are:
  *       0 or empty - No bike information for the trip.
  *       1 - Vehicle being used on this particular trip can accommodate at least one bicycle.
  *       2 - No bicycles are allowed on this trip.
  */
case class Trip(
    @CsvName("route_id")              routeId: String,
    @CsvName("service_id")            serviceId: String,
    @CsvName("trip_id")               id: String, // TODO move to top
    @CsvName("trip_headsign")         headsign: Option[String],
    @CsvName("trip_short_name")       shortName: Option[String],
    @CsvName("direction_id")          directionId: Option[Int],
    @CsvName("block_id")              blockId: Option[String],
    @CsvName("shape_id")              shapeId: Option[String],
    @CsvName("wheelchair_accessible") wheelchairAccessible: Option[Int],
    @CsvName("bikes_allowed")         bikesAllowed: Option[Int]
)

object Trip {
  given CsvRowDecoder[Trip, String] = deriveCsvRowDecoder[Trip]
  given CsvRowEncoder[Trip, String] = deriveCsvRowEncoder[Trip]
}
