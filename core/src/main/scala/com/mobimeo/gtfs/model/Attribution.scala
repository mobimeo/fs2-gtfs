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

case class Attribution(
    @CsvName("attribution_id")
    id: Option[String],
    @CsvName("agency_id")
    agencyId: Option[String],
    @CsvName("route_id")
    routeId: Option[String],
    @CsvName("trip_id")
    tripId: Option[String],
    @CsvName("organization_name")
    organizationName: String,
    @CsvName("is_producer")
    isProducer: Option[Boolean],
    @CsvName("is_operator")
    isOperator: Option[Boolean],
    @CsvName("is_authority")
    isAuthority: Option[Boolean],
    @CsvName("attribution_url")
    url: Option[String],
    @CsvName("attribution_email")
    email: Option[String],
    @CsvName("attribution_phone")
    phone: Option[String]
)

object Attribution {
  given CsvRowDecoder[Attribution, String] = deriveCsvRowDecoder[Attribution]
  given CsvRowEncoder[Attribution, String] = deriveCsvRowEncoder[Attribution]
}
