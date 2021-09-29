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

import enumeratum.EnumEntry
import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class SimpleRouteType(val value: Int) extends IntEnumEntry with EnumEntry
object SimpleRouteType extends IntEnum[SimpleRouteType] with CsvIntEnum[SimpleRouteType] {
  case object Tram       extends SimpleRouteType(0)
  case object Subway     extends SimpleRouteType(1)
  case object Rail       extends SimpleRouteType(2)
  case object Bus        extends SimpleRouteType(3)
  case object Ferry      extends SimpleRouteType(4)
  case object CableTram  extends SimpleRouteType(5)
  case object AerialLift extends SimpleRouteType(6)
  case object Funicular  extends SimpleRouteType(7)
  case object Trolleybus extends SimpleRouteType(11)
  case object Monorail   extends SimpleRouteType(12)

  val values = findValues
}
