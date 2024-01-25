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

import fs2.data.csv.*
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto.*
import java.time.*

/**
  * Stops where vehicles pick up or drop off riders.
  *
  * Also defines stations and station entrances.
  *
  * @param id Identifies a location: stop/platform, station, entrance/exit, generic node or boarding area (see location_type).
  *          Multiple routes may use the same stop_id.
  * @param code Short text or a number that identifies the location for riders.
  *          These codes are often used in phone-based transit information systems or printed on signage to make it easier for riders to get information for a particular location.
  *          The stop_code may be the same as stop_id if it is public facing.
  *          This field should be left empty for locations without a code presented to riders.
  * @param name Name of the location.
  *          The stop_name should match the agency's rider-facing name for the location as printed on a timetable, published online, or represented on signage.
  *          For translations into other languages, use translations.txt.
  *
  *          When the location is a boarding area (location_type=4), the stop_name should contains the name of the boarding area as displayed by the agency.
  *          It could be just one letter (like on some European intercity railway stations), or text like “Wheelchair boarding area” (NYC’s Subway) or “Head of short trains” (Paris’ RER).
  *
  *          Conditionally Required:
  *          - Required for locations which are stops (location_type=0), stations (location_type=1) or entrances/exits (location_type=2).
  *          - Optional for locations which are generic nodes (location_type=3) or boarding areas (location_type=4).
  * @param desc Description of the location that provides useful, quality information.
  *          Should not be a duplicate of stop_name.
  * @param lat Latitude of the location.
  *          For stops/platforms (location_type=0) and boarding area (location_type=4), the coordinates must be the ones of the bus pole — if exists — and otherwise of where the travelers are boarding the vehicle (on the sidewalk or the platform, and not on the roadway or the track where the vehicle stops).
  *
  *          Conditionally Required:
  *          - Required for locations which are stops (location_type=0), stations (location_type=1) or entrances/exits (location_type=2).
  *          - Optional for locations which are generic nodes (location_type=3) or boarding areas (location_type=4).
  * @param lon Longitude of the location.
  *          For stops/platforms (location_type=0) and boarding area (location_type=4), the coordinates must be the ones of the bus pole — if exists — and otherwise of where the travelers are boarding the vehicle (on the sidewalk or the platform, and not on the roadway or the track where the vehicle stops).
  *
  *          Conditionally Required:
  *          - Required for locations which are stops (location_type=0), stations (location_type=1) or entrances/exits (location_type=2).
  *          - Optional for locations which are generic nodes (location_type=3) or boarding areas (location_type=4).
  * @param zoneId Identifies the fare zone for a stop.
  *          If this record represents a station or station entrance, the zone_id is ignored.
  *
  *          Conditionally Required:
  *          - Required if providing fare information using fare_rules.txt
  *          - Optional otherwise.
  * @param url URL of a web page about the location.
  *          This should be different from the agency.agency_url and the routes.route_url field values.
  * @param locationType Location type.
  *          Valid options are:
  *          0 (or blank) - Stop (or Platform). A location where passengers board or disembark from a transit vehicle. Is called a platform when defined within a parent_station.
  *          1 - Station. A physical structure or area that contains one or more platform.
  *          2 - Entrance/Exit. A location where passengers can enter or exit a station from the street.
  *              If an entrance/exit belongs to multiple stations, it may be linked by pathways to both, but the data provider must pick one of them as parent.
  *          3 - Generic Node. A location within a station, not matching any other location_type, that may be used to link together pathways define in pathways.txt.
  *          4 - Boarding Area. A specific location on a platform, where passengers can board and/or alight vehicles.
  * @param parentStation Defines hierarchy between the different locations defined in stops.txt.
  *          It contains the ID of the parent location, as followed:
  *
  *          - Stop/platform (location_type=0): the parent_station field contains the ID of a station.
  *          - Station (location_type=1): this field must be empty.
  *          - Entrance/exit (location_type=2) or generic node (location_type=3):
  *            the parent_station field contains the ID of a station (location_type=1)
  *          - Boarding Area (location_type=4): the parent_station field contains ID of a platform.
  *
  *          Conditionally Required:
  *          - Required for locations which are entrances (location_type=2), generic nodes (location_type=3) or boarding areas (location_type=4).
  *          - Optional for stops/platforms (location_type=0).
  *          - Forbidden for stations (location_type=1).
  * @param timezone Timezone of the location.
  *          If the location has a parent station, it inherits the parent station’s timezone instead of applying its own.
  *          Stations and parentless stops with empty stop_timezone inherit the timezone specified by agency.agency_timezone.
  *          The times provided in stop_times.txt are in the timezone specified by agency.agency_timezone, not stop_timezone.
  *          This ensures that the time values in a trip always increase over the course of a trip, regardless of which timezones the trip crosses.
  * @param wheelchairBoarding Indicates whether wheelchair boardings are possible from the location.
  *          Valid options are:
  *
  *          For parentless stops:
  *          0 or empty - No accessibility information for the stop.
  *          1 - Some vehicles at this stop can be boarded by a rider in a wheelchair.
  *          2 - Wheelchair boarding is not possible at this stop.
  *
  *          For child stops:
  *          0 or empty - Stop will inherit its wheelchair_boarding behavior from the parent station, if specified in the parent.
  *          1 - There exists some accessible path from outside the station to the specific stop/platform.
  *          2 - There exists no accessible path from outside the station to the specific stop/platform.
  *
  *          For station entrances/exits:
  *          0 or empty - Station entrance will inherit its wheelchair_boarding behavior from the parent station, if specified for the parent.
  *          1 - Station entrance is wheelchair accessible.
  *          2 - No accessible path from station entrance to stops/platforms.
  * @param levelId Level of the location. The same level may be used by multiple unlinked stations.
  * @param platformCode Platform identifier for a platform stop (a stop belonging to a station).
  *          This should be just the platform identifier (eg. "G" or "3").
  *          Words like “platform” or "track" (or the feed’s language-specific equivalent) should not be included.
  *          This allows feed consumers to more easily internationalize and localize the platform identifier into other languages.
  */
case class Stop(
    @CsvName("stop_id")             id: String,
    @CsvName("stop_code")           code: Option[String],
    @CsvName("stop_name")           name: Option[String],
    // TODO tts_name
    @CsvName("stop_desc")           desc: Option[String],
    @CsvName("stop_lat")            lat: Option[Double],
    @CsvName("stop_lon")            lon: Option[Double],
    @CsvName("zone_id")             zoneId: Option[String],
    @CsvName("stop_url")            url: Option[String],
    @CsvName("location_type")       locationType: Option[LocationType],
    @CsvName("parent_station")      parentStation: Option[String],
    @CsvName("stop_timezone")       timezone: Option[ZoneId],
    @CsvName("wheelchair_boarding") wheelchairBoarding: Option[Int],
    @CsvName("level_id")            levelId: Option[String],
    @CsvName("platform_code")       platformCode: Option[String]
)

object Stop {
  given CsvRowDecoder[Stop, String] = deriveCsvRowDecoder[Stop]
  given CsvRowEncoder[Stop, String] = deriveCsvRowEncoder[Stop]
}
