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

import enumeratum._

/** Standard GTFS file names. */
sealed trait StandardName extends EnumEntry with EnumEntry.Snakecase {
  override def entryName: String = stableEntryName

  private[this] lazy val stableEntryName: String = s"${super.entryName}.txt"
}

object StandardName extends Enum[StandardName] {
  case object Stops          extends StandardName
  case object Routes         extends StandardName
  case object Trips          extends StandardName
  case object StopTimes      extends StandardName
  case object Agency         extends StandardName
  case object Calendar       extends StandardName
  case object CalendarDates  extends StandardName
  case object FareAttributes extends StandardName
  case object FareRules      extends StandardName
  case object Shapes         extends StandardName
  case object Frequencies    extends StandardName
  case object Transfers      extends StandardName
  case object Pathways       extends StandardName
  case object Levels         extends StandardName
  case object FeedInfo       extends StandardName
  case object Translations   extends StandardName
  case object Attributions   extends StandardName

  def values = findValues
}
