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
package model

import fs2.data.csv._
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto._

/**
  * Transit routes. A route is a group of trips that are displayed to riders as a single service.
  *
  * @param id Identifies a route.
  * @param agencyId Agency for the specified route.
  *
  *          Conditionally Required:
  *          - Required if multiple agencies are defined in agency.txt.
  *          - Recommended otherwise.
  * @param shortName Short name of a route.
  *          Often a short, abstract identifier (e.g., "32", "100X", "Green") that riders use to identify a route.
  *          Both route_short_name and route_long_name may be defined.
  *
  *          Conditionally Required:
  *          - Required if routes.route_long_name is empty.
  *          - Recommended if there is a brief service designation. This should be the commonly-known passenger name of the service, and should be no longer than 12 characters.
  * @param longName Full name of a route.
  *         This name is generally more descriptive than the route_short_name and often includes the route's destination or stop.
  *         Both route_short_name and route_long_name may be defined.
  *
  *         Conditionally Required:
  *         - Required if routes.route_short_name is empty.
  *         - Optional otherwise.
  * @param desc Description of a route that provides useful, quality information.
  *         Should not be a duplicate of route_short_name or route_long_name.
  *
  *         Example: "A" trains operate between Inwood-207 St, Manhattan and Far Rockaway-Mott Avenue, Queens at all times.
  *         Also from about 6AM until about midnight, additional "A" trains operate between Inwood-207 St and Lefferts Boulevard (trains typically alternate between Lefferts Blvd and Far Rockaway).
  * @param tpe Indicates the type of transportation used on a route.
  *
  *         Valid options are:
  *         0 - Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
  *         1 - Subway, Metro. Any underground rail system within a metropolitan area.
  *         2 - Rail. Used for intercity or long-distance travel.
  *         3 - Bus. Used for short- and long-distance bus routes.
  *         4 - Ferry. Used for short- and long-distance boat service.
  *         5 - Cable tram. Used for street-level rail cars where the cable runs beneath the vehicle (e.g., cable car in San Francisco).
  *         6 - Aerial lift, suspended cable car (e.g., gondola lift, aerial tramway). Cable transport where cabins, cars, gondolas or open chairs are suspended by means of one or more cables.
  *         7 - Funicular. Any rail system designed for steep inclines.
  *         11 - Trolleybus. Electric buses that draw power from overhead wires using poles.
  *         12 - Monorail. Railway in which the track consists of a single rail or a beam.
  * @param url URL of a web page about the particular route. Should be different from the agency.agency_url value.
  * @param color Route color designation that matches public facing material.
  *         Defaults to white (FFFFFF) when omitted or left empty.
  *         The color difference between route_color and route_text_color should provide sufficient contrast when viewed on a black and white screen.
  * @param textColor Legible color to use for text drawn against a background of route_color.
  *         Defaults to black (000000) when omitted or left empty.
  *         The color difference between route_color and route_text_color should provide sufficient contrast when viewed on a black and white screen.
  * @param sortOrder Orders the routes in a way which is ideal for presentation to customers.
  *         Routes with smaller route_sort_order values should be displayed first.
  */
case class Route(
  @CsvName("route_id")          id: String,
  @CsvName("agency_id")         agencyId: Option[String],
  @CsvName("route_short_name")  shortName: Option[String],
  @CsvName("route_long_name")   longName: Option[String],
  @CsvName("route_desc")        desc: Option[String],
  @CsvName("route_type")        tpe: RouteType,
  @CsvName("route_url")         url: Option[String],
  @CsvName("route_color")       color: Option[String],
  @CsvName("route_text_color")  textColor: Option[String],
  @CsvName("route_sort_order")  sortOrder: Option[Int]
)

object Route {
  given csvRowDecoder: CsvRowDecoder[Route, String] = deriveCsvRowDecoder[Route]
  given csvRowEncoder: CsvRowEncoder[Route, String] = deriveCsvRowEncoder[Route]
}
