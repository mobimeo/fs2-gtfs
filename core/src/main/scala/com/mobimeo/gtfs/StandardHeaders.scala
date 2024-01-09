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

import cats.data.NonEmptyList

/** Standard GTFS headers for standard files. */
object StandardHeaders {

  val Stops = NonEmptyList.of(
    "stop_id",
    "stop_code",
    "stop_name",
    "stop_desc",
    "stop_lat",
    "stop_lon",
    "zone_id",
    "stop_url",
    "location_type",
    "parent_station",
    "stop_timezone",
    "wheelchair_boarding",
    "level_id",
    "platform_code"
  )

  val Routes = NonEmptyList.of(
    "route_id",
    "agency_id",
    "route_short_name",
    "route_long_name",
    "route_desc",
    "route_type",
    "route_url",
    "route_color",
    "route_text_color",
    "route_sort_order",
    "continuous_pickup",
    "continuous_drop_off"
  )

  val Trips = NonEmptyList.of(
    "route_id",
    "service_id",
    "trip_id",
    "trip_headsign",
    "trip_short_name",
    "direction_id",
    "block_id",
    "shape_id",
    "wheelchair_accessible",
    "bikes_allowed"
  )

  val StopTimes = NonEmptyList.of(
    "trip_id",
    "arrival_time",
    "departure_time",
    "stop_id",
    "stop_sequence",
    "stop_headsign",
    "pickup_type",
    "drop_off_type",
    "continuous_pickup",
    "continuous_drop_off",
    "shape_dist_traveled",
    "timepoint"
  )

  val Agency = NonEmptyList.of(
    "agency_id",
    "agency_name",
    "agency_url",
    "agency_lang",
    "agency_phone",
    "agency_fare_url",
    "agency_email"
  )

  val Calendar = NonEmptyList.of(
    "service_id",
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday",
    "start_date",
    "end_date"
  )

  val CalendarDates = NonEmptyList.of(
    "service_id",
    "date",
    "exception_type"
  )

  val FareAttributes = NonEmptyList.of(
    "fare_id",
    "price",
    "currency_type",
    "payment_method",
    "transfers",
    "agency_id",
    "transfer_duration"
  )

  val FareRules = NonEmptyList.of(
    "fare_id",
    "route_id",
    "origin_id",
    "destination_id",
    "contains_id"
  )

  val Shapes = NonEmptyList.of(
    "shape_id",
    "shape_pt_lat",
    "shape_pt_lon",
    "shape_pt_sequence",
    "shape_dist_traveled"
  )

  val Frequencies = NonEmptyList.of(
    "trip_id",
    "start_time",
    "end_time",
    "headway_secs",
    "exact_times"
  )

  val Transfers = NonEmptyList.of(
    "from_stop_id",
    "to_stop_id",
    "transfer_type",
    "min_transfer_time"
  )

  val Pathways = NonEmptyList.of(
    "pathway_id",
    "from_stop_id",
    "to_stop_id",
    "pathway_mode",
    "is_bidirectional",
    "length",
    "traversal_time",
    "stair_count",
    "max_slope",
    "min_width",
    "signposted_as",
    "reversed_signposted_as"
  )

  val Levels = NonEmptyList.of(
    "level_id",
    "level_index",
    "level_name"
  )

  val FeedInfo = NonEmptyList.of(
    "feed_publisher_name",
    "feed_publisher_url",
    "feed_lang",
    "default_lang",
    "feed_start_date",
    "feed_end_date",
    "feed_version",
    "feed_contact_email",
    "feed_contact_url"
  )

  val Translations = NonEmptyList.of(
    "table_name",
    "field_name",
    "language",
    "translation",
    "record_id",
    "record_sub_id",
    "field_value"
  )

  val Attributions = NonEmptyList.of(
    "attribution_id",
    "agency_id",
    "route_id",
    "trip_id",
    "organization_name",
    "is_producer",
    "is_operator",
    "is_authority",
    "attribution_url",
    "attribution_email",
    "attribution_phone"
  )

  /** Returns the set of standard headers for the given file name. */
  def standardHeadersFor(file: StandardName): NonEmptyList[String] =
    file match {
      case StandardName.Stops          => Stops
      case StandardName.Routes         => Routes
      case StandardName.Trips          => Trips
      case StandardName.StopTimes      => StopTimes
      case StandardName.Agency         => Agency
      case StandardName.Calendar       => Calendar
      case StandardName.CalendarDates  => CalendarDates
      case StandardName.FareAttributes => FareAttributes
      case StandardName.FareRules      => FareRules
      case StandardName.Shapes         => Shapes
      case StandardName.Frequencies    => Frequencies
      case StandardName.Transfers      => Transfers
      case StandardName.Pathways       => Pathways
      case StandardName.Levels         => Levels
      case StandardName.FeedInfo       => FeedInfo
      case StandardName.Translations   => Translations
      case StandardName.Attributions   => Attributions
    }

}
