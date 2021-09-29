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

enum SimpleRouteType(val value: Int) extends IntEnumEntry {
  case Tram       extends SimpleRouteType(0)
  case Subway     extends SimpleRouteType(1)
  case Rail       extends SimpleRouteType(2)
  case Bus        extends SimpleRouteType(3)
  case Ferry      extends SimpleRouteType(4)
  case CableTram  extends SimpleRouteType(5)
  case AerialLift extends SimpleRouteType(6)
  case Funicular  extends SimpleRouteType(7)
  case Trolleybus extends SimpleRouteType(11)
  case Monorail   extends SimpleRouteType(12)
}

object SimpleRouteType extends CsvIntEnum[SimpleRouteType]
