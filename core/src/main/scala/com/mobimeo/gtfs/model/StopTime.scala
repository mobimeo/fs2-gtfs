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
import Timepoint
import PickupOrDropOffType

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
  given CsvRowDecoder[StopTime, String] = deriveCsvRowDecoder[StopTime]
  given CsvRowEncoder[StopTime, String] = deriveCsvRowEncoder[StopTime]
}

