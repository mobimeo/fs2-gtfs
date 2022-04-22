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

enum StandardName(val entryName: String) {
  case Stops extends StandardName("stops.txt")
  case Routes extends StandardName("routes.txt")
  case Trips extends StandardName("trips.txt")
  case StopTimes extends StandardName("stop_times.txt")
  case Agency extends StandardName("agency.txt")
  case Calendar extends StandardName("calendar.txt")
  case CalendarDates extends StandardName("calendar_dates.txt")
  case FareAttributes extends StandardName("fare_attributes.txt")
  case FareRules extends StandardName("fare_rules.txt")
  case Shapes extends StandardName("shapes.txt")
  case Frequencies extends StandardName("frequencies.txt")
  case Transfers extends StandardName("transfers.txt")
  case Pathways extends StandardName("pathways.txt")
  case Levels extends StandardName("levels.txt")
  case FeedInfo extends StandardName("feed_info.txt")
  case Translations extends StandardName("translations.txt")
  case Attributions extends StandardName("attributions.txt")
}
