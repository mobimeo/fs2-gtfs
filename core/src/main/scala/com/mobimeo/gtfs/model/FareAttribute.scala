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

package com.mobimeo.gtfs
package model

import fs2.data.csv._
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto._
import java.{util => ju}

case class FareAttribute(
    @CsvName("fare_id")
    id: String,
    price: Double,
    @CsvName("currency_type")
    currency: ju.Currency,
    @CsvName("payment_method")
    paymentMethod: PaymentMethod,
    transfers: Option[Int],
    @CsvName("agency_id")
    agencyId: Option[String],
    @CsvName("transfer_duration")
    transferDuration: Option[Int]
)

object FareAttribute {
  given CsvRowDecoder[FareAttribute, String] = deriveCsvRowDecoder[FareAttribute]
  given CsvRowEncoder[FareAttribute, String] = deriveCsvRowEncoder[FareAttribute]
}
