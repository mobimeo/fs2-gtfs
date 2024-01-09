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

case class FeedInfo(
  @CsvName("feed_version")
  version: Option[String],
  @CsvName("feed_publisher_name")
  publisherName: String,
  @CsvName("feed_publisher_url")
  publisherUrl: String,
  @CsvName("feed_lang")
  lang: String,
  @CsvName("default_lang")
  defaultLang: Option[String],
  @CsvName("feed_start_date")
  startDate: Option[LocalDate],
  @CsvName("feed_end_date")
  endDate: Option[LocalDate],
  @CsvName("feed_contact_email")
  contactEmail: Option[String],
  @CsvName("feed_contact_url")
  contactUrl: Option[String]
)

object FeedInfo {
  given CsvRowDecoder[FeedInfo, String] = deriveCsvRowDecoder[FeedInfo]
  given CsvRowEncoder[FeedInfo, String] = deriveCsvRowEncoder[FeedInfo]
}
