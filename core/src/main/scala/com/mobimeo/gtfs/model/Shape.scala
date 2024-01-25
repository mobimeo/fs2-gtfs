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

/**
  * Rules for mapping vehicle travel paths, sometimes referred to as route alignments.
  *
  * @param id Identifies a shape.
  * @param lat Latitude of a shape point. Each record in shapes.txt represents a shape point used to define the shape.
  * @param lon Longitude of a shape point.
  * @param sequence Sequence in which the shape points connect to form the shape.
  *
  *       Values must increase along the trip but do not need to be consecutive.
  *
  *       Example: If the shape "A_shp" has three points in its definition,
  *       the shapes.txt file might contain these records to define the shape:
  *       shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence
  *       A_shp,37.61956,-122.48161,0
  *       A_shp,37.64430,-122.41070,6
  *       A_shp,37.65863,-122.30839,11
  * @param distTraveled Actual distance traveled along the shape from the first shape point to the point specified in this record.
  *
  *       Used by trip planners to show the correct portion of the shape on a map.
  *       Values must increase along with shape_pt_sequence; they must not be used to show reverse travel along a route.
  *       Distance units must be consistent with those used in stop_times.txt.
  *
  *       Recommended for routes that have looping or inlining (the vehicle crosses or travels over the same portion of alignment in one trip).
  *
  *       If a vehicle retraces or crosses the route alignment at points in the course of a trip,
  *       shape_dist_traveled is important to clarify how portions of the points in shapes.txt line up
  *       correspond with records in stop_times.txt.
  *
  *       Example: If a bus travels along the three points defined above for A_shp,
  *       the additional shape_dist_traveled values (shown here in kilometers) would look like this:
  *       shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled
  *       A_shp,37.61956,-122.48161,0,0
  *       A_shp,37.64430,-122.41070,6,6.8310
  *       A_shp,37.65863,-122.30839,11,15.8765
  */
case class Shape(
  @CsvName("shape_id")            id: String,
  @CsvName("shape_pt_lat")        lat: Double,
  @CsvName("shape_pt_lon")        lon: Double,
  @CsvName("shape_pt_sequence")   sequence: Int,
  @CsvName("shape_dist_traveled") distTraveled: Option[Double]
)

object Shape {
  given CsvRowDecoder[Shape, String] = deriveCsvRowDecoder[Shape]
  given CsvRowEncoder[Shape, String] = deriveCsvRowEncoder[Shape]
}
