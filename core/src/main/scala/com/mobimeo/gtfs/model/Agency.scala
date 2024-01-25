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
import java.net.URL
import java.time.*

/**
  * Transit agencies with service.
  *
  * Conditionally Required:
  * - Required when the dataset contains data for multiple transit agencies.
  * - Recommended otherwise.
  *
  * @param id Identifies a transit brand which is often synonymous with a transit agency.
  *          Note that in some cases, such as when a single agency operates multiple separate services,
  *          agencies and brands are distinct.
  *          This document uses the term "agency" in place of "brand".
  *          A dataset may contain data from multiple agencies.
  * @param name Full name of the transit agency
  * @param url URL of the transit agency
  * @param timezone Timezone where the transit agency is located.
  *          If multiple agencies are specified in the dataset, each must have the same agency_timezone.
  * @param language Primary language used by this transit agency.
  *          Should be provided to help GTFS consumers choose capitalization rules and other language-specific settings for the dataset.
  * @param phone A voice telephone number for the specified agency.
  *          This field is a string value that presents the telephone number as typical for the agency's service area.
  *          It may contain punctuation marks to group the digits of the number.
  *          Dialable text (for example, TriMet's "503-238-RIDE") is permitted, but the field must not contain any other descriptive text.
  * @param fareUrl URL of a web page that allows a rider to purchase tickets or other fare instruments for that agency online.
  * @param email Email address actively monitored by the agency’s customer service department.
  *          This email address should be a direct contact point where transit riders can reach a customer service representative at the agency.
  * @param ticketingDeepLinkId
  */
case class Agency(
    @CsvName("agency_id")               id: String,
    @CsvName("agency_name")             name: String,
    @CsvName("agency_url")              url: Option[URL],
    @CsvName("agency_timezone")         timezone: ZoneId, // TODO move to dataset table
    @CsvName("agency_lang")             language: Option[String],
    @CsvName("agency_phone")            phone: Option[String],
    @CsvName("agency_fare_url")         fareUrl: Option[String],
    @CsvName("agency_email")            email: Option[String],
    @CsvName("ticketing_deep_link_id")  ticketingDeepLinkId: Option[String] // TODO remove?
)

object Agency {
  given CsvRowDecoder[Agency, String] = deriveCsvRowDecoder[Agency]
  given CsvRowEncoder[Agency, String] = deriveCsvRowEncoder[Agency]
}
