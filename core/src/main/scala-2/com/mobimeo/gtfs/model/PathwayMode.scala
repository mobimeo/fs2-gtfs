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

sealed abstract class PathwayMode(val value: Int) extends IntEnumEntry
object PathwayMode extends IntEnum[PathwayMode] with CsvIntEnum[PathwayMode] {
  case object Walkway        extends PathwayMode(1)
  case object Stairs         extends PathwayMode(2)
  case object MovingSidewalk extends PathwayMode(3)
  case object Escalator      extends PathwayMode(4)
  case object Elevator       extends PathwayMode(5)
  case object FareGate       extends PathwayMode(6)
  case object ExitGate       extends PathwayMode(7)

  val values = findValues
}
