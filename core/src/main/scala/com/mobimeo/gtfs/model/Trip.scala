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

case class Trip(
    @CsvName("route_id")              routeId: String,
    @CsvName("service_id")            serviceId: String,
    @CsvName("trip_id")               id: String,
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
