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

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class Timepoint(val value: Int) extends IntEnumEntry
object Timepoint extends IntEnum[Timepoint] with CsvIntEnum[Timepoint] {
  case object Approximate extends Timepoint(0)
  case object Exact       extends Timepoint(1)

  val values = findValues
}
