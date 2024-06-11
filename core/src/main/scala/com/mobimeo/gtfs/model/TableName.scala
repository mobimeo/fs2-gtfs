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

enum TableName(val entryName: String) extends EnumEntry {
  case Agency       extends TableName("agency")
  case Stops        extends TableName("stops")
  case Routes       extends TableName("routes")
  case Trips        extends TableName("trips")
  case StopTimes    extends TableName("stop_times")
  case FeedInfo     extends TableName("feed_info")
  case Pathways     extends TableName("pathways")
  case Levels       extends TableName("levels")
  case Attributions extends TableName("attributions")
}

object TableName extends CsvEnum[TableName]
