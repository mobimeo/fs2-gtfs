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

/**
  * Rules to apply fares for itineraries.
  *
  * @param fareId Identifies a fare class.
  * @param routeId Identifies a route associated with the fare class.
  *
  * If several routes with the same fare attributes exist, create a record in fare_rules.txt for each route.
  *
  * Example: If fare class "b" is valid on route "TSW" and "TSE",
  * the fare_rules.txt file would contain these records for the fare class:
  * fare_id,route_id
  * b,TSW
  * b,TSE
  * @param originId Identifies an origin zone.
  *
  * If a fare class has multiple origin zones, create a record in fare_rules.txt for each origin_id.
  *
  * Example: If fare class "b" is valid for all travel originating from either zone "2" or zone "8",
  * the fare_rules.txt file would contain these records for the fare class:
  * fare_id,...,origin_id
  * b,...,2
  * b,...,8
  * @param destinationId Identifies a destination zone.
  *
  * If a fare class has multiple destination zones, create a record in fare_rules.txt for each destination_id.
  *
  * Example: The origin_id and destination_id fields could be used together
  * to specify that fare class "b" is valid for travel between zones 3 and 4,
  * and for travel between zones 3 and 5, the fare_rules.txt file would contain these records for the fare class:
  * fare_id,...,origin_id,destination_id
  * b,...,3,4
  * b,...,3,5
  * @param containsId Identifies the zones that a rider will enter while using a given fare class.
  *
  * Used in some systems to calculate correct fare class.
  *
  * Example: If fare class "c" is associated with all travel on the GRT route
  * that passes through zones 5, 6, and 7 the fare_rules.txt would contain these records:
  * fare_id,route_id,...,contains_id
  * c,GRT,...,5
  * c,GRT,...,6
  * c,GRT,...,7
  *
  * Because all contains_id zones must be matched for the fare to apply,
  * an itinerary that passes through zones 5 and 6 but not zone 7 would not have fare class "c".
  * For more detail, see https://code.google.com/p/googletransitdatafeed/wiki/FareExamples
  * in the GoogleTransitDataFeed project wiki.
  */
case class FareRules(
    @CsvName("fare_id")
    fareId: String,
    @CsvName("route_id")
    routeId: Option[String],
    @CsvName("origin_id")
    originId: Option[String],
    @CsvName("destination_id")
    destinationId: Option[String],
    @CsvName("contains_id")
    containsId: Option[String]
)

object FareRules {
  given CsvRowDecoder[FareRules, String] = deriveCsvRowDecoder[FareRules]
  given CsvRowEncoder[FareRules, String] = deriveCsvRowEncoder[FareRules]
}
