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
import java.time.LocalTime

/**
  * Times that a vehicle arrives at and departs from stops for each trip.
  *
  * TODO continuous_pickup param shapeDistTraveled Indicates that the rider can board the transit vehicle at any point along the vehicle’s travel path as described by shapes.txt, from this stop_time to the next stop_time in the trip’s stop_sequence.
  *
  *           Valid options are:
  *           0 - Continuous stopping pickup.
  *           1 or empty - No continuous stopping pickup.
  *           2 - Must phone agency to arrange continuous stopping pickup.
  *           3 - Must coordinate with driver to arrange continuous stopping pickup.
  *
  *           If this field is populated, it overrides any continuous pickup behavior defined in routes.txt.
  *           If this field is empty, the stop_time inherits any continuous pickup behavior defined in routes.txt.
  *
  * TODO continuous_drop_off
  * TODO shape_dist_traveled
  *
  * @param tripId Identifies a trip.
  * @param arrivalTime Arrival time at the stop (defined by stop_times.stop_id) for a specific trip (defined by stop_times.trip_id) in the time zone specified by agency.agency_timezone, not stops.stop_timezone.
  *
  *           If there are not separate times for arrival and departure at a stop, arrival_time and departure_time should be the same.
  *
  *           For times occurring after midnight on the service day, enter the time as a value greater than 24:00:00 in HH:MM:SS.
  *
  *           If exact arrival and departure times (timepoint=1 or empty) are not available, estimated or interpolated arrival and departure times (timepoint=0) should be provided.
  *
  *           Conditionally Required:
  *           - Required for the first and last stop in a trip (defined by stop_times.stop_sequence).
  *           - Required for timepoint=1.
  *           - Optional otherwise.
  * @param departureTime Departure time from the stop (defined by stop_times.stop_id) for a specific trip (defined by stop_times.trip_id) in the time zone specified by agency.agency_timezone, not stops.stop_timezone.
  *
  *           If there are not separate times for arrival and departure at a stop, arrival_time and departure_time should be the same.
  *
  *           For times occurring after midnight on the service day, enter the time as a value greater than 24:00:00 in HH:MM:SS.
  *
  *           If exact arrival and departure times (timepoint=1 or empty) are not available, estimated or interpolated arrival and departure times (timepoint=0) should be provided.
  *
  *           Conditionally Required:
  *           - Required for timepoint=1.
  *           - Optional otherwise.
  * @param stopId Identifies the serviced stop. All stops serviced during a trip must have a record in stop_times.txt. Referenced locations must be stops/platforms, i.e. their stops.location_type value must be 0 or empty. A stop may be serviced multiple times in the same trip, and multiple trips and routes may service the same stop.
  * @param stopSequence Order of stops for a particular trip.
  *           The values must increase along the trip but do not need to be consecutive.
  *
  *           Example: The first location on the trip could have a stop_sequence=1,
  *           the second location on the trip could have a stop_sequence=23,
  *           the third location could have a stop_sequence=40, and so on.
  * @param stopHeadsign Text that appears on signage identifying the trip's destination to riders.
  *
  *           This field overrides the default trips.trip_headsign when the headsign changes between stops.
  *           If the headsign is displayed for an entire trip, trips.trip_headsign should be used instead.
  *
  *           A stop_headsign value specified for one stop_time does not apply to subsequent stop_times in the same trip.
  *           If you want to override the trip_headsign for multiple stop_times in the same trip, the stop_headsign value must be repeated in each stop_time row.
  * @param pickupType Indicates pickup method.
  *
  *           Valid options are:
  *           0 or empty - Regularly scheduled pickup.
  *           1 - No pickup available.
  *           2 - Must phone agency to arrange pickup.
  *           3 - Must coordinate with driver to arrange pickup.
  * @param dropOffType Indicates drop off method.
  *
  *           Valid options are:
  *           0 or empty - Regularly scheduled drop off.
  *           1 - No drop off available.
  *           2 - Must phone agency to arrange drop off.
  *           3 - Must coordinate with driver to arrange drop off.
  * @param timepoint Indicates if arrival and departure times for a stop are strictly adhered to by the vehicle or if they are instead approximate and/or interpolated times.
  *
  *           This field allows a GTFS producer to provide interpolated stop-times, while indicating that the times are approximate.
  *
  *           Valid options are:
  *           0 - Times are considered approximate.
  *           1 or empty - Times are considered exact.
  */
case class StopTime(
    @CsvName("trip_id")             tripId: String,
    @CsvName("arrival_time")        arrivalTime: LocalTime,
    @CsvName("departure_time")      departureTime: LocalTime,
    @CsvName("stop_id")             stopId: String,
    @CsvName("stop_sequence")       stopSequence: Int,
    @CsvName("stop_headsign")       stopHeadsign: Option[String],
    @CsvName("pickup_type")         pickupType: Option[PickupOrDropOffType],
    @CsvName("drop_off_type")       dropOffType: Option[PickupOrDropOffType],
    @CsvName("shape_dist_traveled") shapeDistTraveled: Option[Double],
    timepoint: Option[Timepoint]
)

object StopTime {
  given CsvRowDecoder[StopTime, String] = deriveCsvRowDecoder[StopTime]
  given CsvRowEncoder[StopTime, String] = deriveCsvRowEncoder[StopTime]
}
