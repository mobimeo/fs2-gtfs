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
import java.time.*

/**
  * Service dates specified using a weekly schedule with start and end dates.
  *
  * Conditionally Required:
  * - Required unless all dates of service are defined in calendar_dates.txt.
  * - Optional otherwise.
  *
  * @param serviceId Identifies a set of dates when service is available for one or more routes.
  * @param monday Indicates whether the service operates on all Mondays in the date range specified by the start_date and end_date fields.
  *           Note that exceptions for particular dates may be listed in calendar_dates.txt.
  *
  *           Valid options are:
  *           1 - Service is available for all Mondays in the date range.
  *           0 - Service is not available for Mondays in the date range.
  * @param tuesday Functions in the same way as monday except applies to Tuesdays.
  * @param wednesday Functions in the same way as monday except applies to Wednesdays.
  * @param thursday Functions in the same way as monday except applies to Thursdays.
  * @param friday Functions in the same way as monday except applies to Fridays.
  * @param saturday Functions in the same way as monday except applies to Saturdays.
  * @param sunday Functions in the same way as monday except applies to Sundays.
  * @param startDate Start service day for the service interval.
  * @param endDate End service day for the service interval. This service day is included in the interval.
  */
case class Calendar(
    @CsvName("service_id")
    serviceId: String,
    monday: Availability,
    tuesday: Availability,
    wednesday: Availability,
    thursday: Availability,
    friday: Availability,
    saturday: Availability,
    sunday: Availability,
    @CsvName("start_date")
    startDate: LocalDate,
    @CsvName("end_date")
    endDate: LocalDate
)

object Calendar {
  given CsvRowDecoder[Calendar, String] = deriveCsvRowDecoder[Calendar]
  given CsvRowEncoder[Calendar, String] = deriveCsvRowEncoder[Calendar]
}
