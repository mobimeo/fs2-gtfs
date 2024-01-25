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
  * Exceptions for the services defined in the calendar.txt.
  *
  * Conditionally Required:
  * - Required if calendar.txt is omitted. In which case calendar_dates.txt must contain all dates of service.
  * - Optional otherwise.
  *
  * @param serviceId Identifies a set of dates when a service exception occurs for one or more routes.
  *
  *       Each (service_id, date) pair may only appear once in calendar_dates.txt
  *       if using calendar.txt and calendar_dates.txt in conjunction.
  *       If a service_id value appears in both calendar.txt and calendar_dates.txt,
  *       the information in calendar_dates.txt modifies the service information specified in calendar.txt.
  * @param date Date when service exception occurs.
  * @param exceptionType Indicates whether service is available on the date specified in the date field.
  *
  *       Valid options are:
  *       1 - Service has been added for the specified date.
  *       2 - Service has been removed for the specified date.
  *
  *       Example: Suppose a route has one set of trips available on holidays and another set of trips available on all other days.
  *       One service_id could correspond to the regular service schedule and another service_id could correspond to the holiday schedule.
  *       For a particular holiday, the calendar_dates.txt file could be used to add the holiday to the holiday service_id and
  *       to remove the holiday from the regular service_id schedule.
  */
case class CalendarDate(
    @CsvName("service_id")      serviceId: String,
    @CsvName("date")            date: LocalDate,
    @CsvName("exception_type")  exceptionType: ExceptionType
)

object CalendarDate {
  given CsvRowDecoder[CalendarDate, String] = deriveCsvRowDecoder[CalendarDate]
  given CsvRowEncoder[CalendarDate, String] = deriveCsvRowEncoder[CalendarDate]
}
