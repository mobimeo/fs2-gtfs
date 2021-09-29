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

import enumeratum.{Enum, EnumEntry}

sealed trait TableName extends EnumEntry with EnumEntry.Snakecase
object TableName extends Enum[TableName] with CsvEnum[TableName] {
  case object Agency       extends TableName
  case object Stops        extends TableName
  case object Routes       extends TableName
  case object Trips        extends TableName
  case object StopTimes    extends TableName
  case object FeedInfo     extends TableName
  case object Pathways     extends TableName
  case object Levels       extends TableName
  case object Attributions extends TableName

  val values = findValues
}
