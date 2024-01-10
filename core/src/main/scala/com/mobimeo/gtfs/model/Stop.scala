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

case class Stop(
    @CsvName("stop_id") id: String,
    @CsvName("stop_code") code: Option[String],
    @CsvName("stop_name") name: Option[String],
    @CsvName("stop_desc") desc: Option[String],
    @CsvName("stop_lat") lat: Option[Double],
    @CsvName("stop_lon") lon: Option[Double],
    @CsvName("zone_id") zoneId: Option[String],
    @CsvName("stop_url") url: Option[String],
    @CsvName("location_type") locationType: Option[LocationType],
    @CsvName("parent_station") parentStation: Option[String],
    @CsvName("stop_timezone") timezone: Option[ZoneId],
    @CsvName("wheelchair_boarding") wheelchairBoarding: Option[Int],
    @CsvName("level_id") levelId: Option[String],
    @CsvName("platform_code") platformCode: Option[String]
)

object Stop {
  given CsvRowDecoder[Stop, String] = deriveCsvRowDecoder[Stop]
  given CsvRowEncoder[Stop, String] = deriveCsvRowEncoder[Stop]
}
