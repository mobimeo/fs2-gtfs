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

case class Level(
    @CsvName("level_id")
    id: String,
    @CsvName("level_index")
    index: Double,
    @CsvName("level_name")
    name: Option[String]
)

object Level {
  given CsvRowDecoder[Level, String] = deriveCsvRowDecoder[Level]
  given CsvRowEncoder[Level, String] = deriveCsvRowEncoder[Level]
}
