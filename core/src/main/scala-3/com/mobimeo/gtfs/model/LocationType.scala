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

enum LocationType(val value: Int) extends IntEnumEntry {
  case Stop extends LocationType(0)
  case Station extends LocationType(1)
  case Entrance extends LocationType(2)
  case GenericNode extends LocationType(3)
  case BoardingArea extends LocationType(4)
}

object LocationType extends OrdinalBasedCsvIntEnum[LocationType]
