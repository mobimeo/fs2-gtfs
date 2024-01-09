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

case class Agency(
    @CsvName("agency_id")
    id: String,
    @CsvName("agency_name")
    name: String,
    @CsvName("agency_url")
    url: Option[String],
    @CsvName("agency_timezone")
    timezone: ZoneId,
    @CsvName("agency_lang")
    language: Option[String],
    @CsvName("agency_phone")
    phone: Option[String],
    @CsvName("agency_fare_url")
    fareUrl: Option[String],
    @CsvName("agency_email")
    email: Option[String],
    @CsvName("ticketing_deep_link_id")
    ticketingDeepLinkId: Option[String]
)

object Agency {
  given CsvRowDecoder[Agency, String] = deriveCsvRowDecoder[Agency]
  given CsvRowEncoder[Agency, String] = deriveCsvRowEncoder[Agency]
}
