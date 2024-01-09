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

case class Route[RouteType](
  @CsvName("route_id")
  id: String,
  @CsvName("agency_id")
  agencyId: Option[String],
  @CsvName("route_short_name")
  shortName: Option[String],
  @CsvName("route_long_name")
  longName: Option[String],
  @CsvName("route_desc")
  desc: Option[String],
  @CsvName("route_type")
  tpe: RouteType,
  @CsvName("route_url")
  url: Option[String],
  @CsvName("route_color")
  color: Option[String],
  @CsvName("route_text_color")
  textColor: Option[String],
  @CsvName("route_sort_order")
  sortOrder: Option[Int]
)

object Route {
  given csvRowDecoder[RouteType](using CellDecoder[RouteType]): CsvRowDecoder[Route[RouteType], String] =
    deriveCsvRowDecoder[Route[RouteType]]

  given csvRowEncoder[RouteType](using CellEncoder[RouteType]): CsvRowEncoder[Route[RouteType], String] =
    deriveCsvRowEncoder[Route[RouteType]]
}
