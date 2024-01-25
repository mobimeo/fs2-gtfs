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
  * Rules for making connections at transfer points between routes.
  *
  * @param fromStopId
  * @param toStopId
  * @param transferType
  * @param minTransferTime
  */
case class Transfer(
    @CsvName("from_stop_id")      fromStopId: String,
    @CsvName("to_stop_id")        toStopId: String,
    @CsvName("transfer_type")     transferType: TransferType,
    @CsvName("min_transfer_time") minTransferTime: Option[Int]
)

object Transfer {
  given CsvRowDecoder[Transfer, String] = deriveCsvRowDecoder[Transfer]
  given CsvRowEncoder[Transfer, String] = deriveCsvRowEncoder[Transfer]
}
