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
  given CsvRowDecoder[Pathway, String] = deriveCsvRowDecoder[Pathway]
  given CsvRowEncoder[Pathway, String] = deriveCsvRowEncoder[Pathway]
}
