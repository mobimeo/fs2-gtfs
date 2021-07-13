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

import fs2._

/** Represents a GTFS container. Can be used to access the content of the different
  * kind of data in it. This is an abstract API giving basic access to the data without
  * presuming in what way they are stored.
  */
trait Gtfs[F[_], Decoder[_], Encoder[_]] {

  /** Whether the GTFS file contains the given file name. */
  def hasFile(name: String): F[Boolean]

  /** Whether the GTFS file contains the given file name. */
  def hasFile(name: StandardName): F[Boolean] =
    hasFile(name.entryName)

  /** Whether the GTFS file contains the agency file. */
  def hasAgency: F[Boolean] =
    hasFile(StandardName.Agency)

  /** Whether the GTFS file contains the stops file. */
  def hasStops: F[Boolean] =
    hasFile(StandardName.Stops)

  /** Whether the GTFS file contains the routes file. */
  def hasRoutes: F[Boolean] =
    hasFile(StandardName.Routes)

  /** Whether the GTFS file contains the trips file. */
  def hasTrips: F[Boolean] =
    hasFile(StandardName.Trips)

  /** Whether the GTFS file contains the stop_times file. */
  def hasStopTimes: F[Boolean] =
    hasFile(StandardName.StopTimes)

  /** Whether the GTFS file contains the calendar file. */
  def hasCalendar: F[Boolean] =
    hasFile(StandardName.Calendar)

  /** Whether the GTFS file contains the calendar_dates file. */
  def hasCalendarDates: F[Boolean] =
    hasFile(StandardName.CalendarDates)

  /** Whether the GTFS file contains the fare_attributes file. */
  def hasFareAttributes: F[Boolean] =
    hasFile(StandardName.FareAttributes)

  /** Whether the GTFS file contains the fare_rules file. */
  def hasFareRules: F[Boolean] =
    hasFile(StandardName.FareRules)

  /** Whether the GTFS file contains the shapes file. */
  def hasShapes: F[Boolean] =
    hasFile(StandardName.Shapes)

  /** Whether the GTFS file contains the frequencies file. */
  def hasFrequencies: F[Boolean] =
    hasFile(StandardName.Frequencies)

  /** Whether the GTFS file contains the transfers file. */
  def hasTransfers: F[Boolean] =
    hasFile(StandardName.Transfers)

  /** Whether the GTFS file contains the pathways file. */
  def hasPathways: F[Boolean] =
    hasFile(StandardName.Pathways)

  /** Whether the GTFS file contains the levels file. */
  def hasLevels: F[Boolean] =
    hasFile(StandardName.Levels)

  /** Whether the GTFS file contains the feed_info file. */
  def hasFeedInfo: F[Boolean] =
    hasFile(StandardName.FeedInfo)

  /** Whether the GTFS file contains the translations file. */
  def hasTranslations: F[Boolean] =
    hasFile(StandardName.Translations)

  /** Whether the GTFS file contains the attributions file. */
  def hasAttributions: F[Boolean] =
    hasFile(StandardName.Attributions)

  /** Namespace containing operators and pipes to read content of a GTFS file access
    * stream.
    */
  val read: GtfsRead[F, Decoder]

  /** Namespace containing operators and pipes to save the Result of a GTFS stream.
    * This can be used to save the result of transformations.
    *
    * Once saved, the content of the GTFS file is modified, subsequent accesses
    * to the same file in the same GTFS file will contain modications.
    */
  val write: GtfsWrite[F, Encoder]

}

trait GtfsRead[F[_], Decoder[_]] {

  /** Gives access to the content of CSV file `name`.
    *
    * For instance `file("calendar.txt")`.
    */
  def file[R](name: String)(implicit decoder: Decoder[R]): Stream[F, R]

  /** Gives access to the content of CSV file `name`. */
  def file[R](name: StandardName)(implicit decoder: Decoder[R]): Stream[F, R] =
    file(name.entryName)

  // aliases for standard GTFS files

  def stops[S](implicit decoder: Decoder[S]): Stream[F, S] =
    file(StandardName.Stops)

  def routes[R](implicit decoder: Decoder[R]): Stream[F, R] =
    file(StandardName.Routes)

  def trips[T](implicit decoder: Decoder[T]): Stream[F, T] =
    file(StandardName.Trips)

  def stopTimes[S](implicit decoder: Decoder[S]): Stream[F, S] =
    file(StandardName.StopTimes)

  def agencies[A](implicit decoder: Decoder[A]): Stream[F, A] =
    file(StandardName.Agency)

  def calendar[C](implicit decoder: Decoder[C]): Stream[F, C] =
    file(StandardName.Calendar)

  def calendarDates[C](implicit decoder: Decoder[C]): Stream[F, C] =
    file(StandardName.CalendarDates)

  def fareAttributes[A](implicit decoder: Decoder[A]): Stream[F, A] =
    file(StandardName.FareAttributes)

  def fareRules[R](implicit decoder: Decoder[R]): Stream[F, R] =
    file(StandardName.FareRules)

  def shapes[S](implicit decoder: Decoder[S]): Stream[F, S] =
    file(StandardName.Shapes)

  def frequencies[S](implicit decoder: Decoder[S]): Stream[F, S] =
    file(StandardName.Frequencies)

  def transfers[T](implicit decoder: Decoder[T]): Stream[F, T] =
    file(StandardName.Transfers)

  def pathways[P](implicit decoder: Decoder[P]): Stream[F, P] =
    file(StandardName.Pathways)

  def levels[L](implicit decoder: Decoder[L]): Stream[F, L] =
    file(StandardName.Levels)

  def feedInfo[I](implicit decoder: Decoder[I]): Stream[F, I] =
    file(StandardName.FeedInfo)

  def translations[T](implicit decoder: Decoder[T]): Stream[F, T] =
    file(StandardName.Translations)

  def attributions[A](implicit decoder: Decoder[A]): Stream[F, A] =
    file(StandardName.Attributions)

}

trait GtfsWrite[F[_], Encoder[_]] {

  /** Gives access to the pipe to save in file `name`.
    *
      * For instance `file("agency.txt")`.
    */
  def file[T](name: String)(implicit encoder: Encoder[T]): Pipe[F, T, Nothing]

  /** Gives access to the pipe to save in file `name`. */
  def file[T](name: StandardName)(implicit encoder: Encoder[T]): Pipe[F, T, Nothing] =
    file(name.entryName)

  // aliases for standard GTFS files

  def stops[S](implicit encoder: Encoder[S]): Pipe[F, S, Nothing] =
    file(StandardName.Stops)

  def routes[R](implicit encoder: Encoder[R]): Pipe[F, R, Nothing] =
    file(StandardName.Routes)

  def trips[T](implicit encoder: Encoder[T]): Pipe[F, T, Nothing] =
    file(StandardName.Trips)

  def stopTimes[S](implicit encoder: Encoder[S]): Pipe[F, S, Nothing] =
    file(StandardName.StopTimes)

  def agencies[A](implicit encoder: Encoder[A]): Pipe[F, A, Nothing] =
    file(StandardName.Agency)

  def calendar[C](implicit encoder: Encoder[C]): Pipe[F, C, Nothing] =
    file(StandardName.Calendar)

  def calendarDates[C](implicit encoder: Encoder[C]): Pipe[F, C, Nothing] =
    file(StandardName.CalendarDates)

  def fareAttributes[A](implicit encoder: Encoder[A]): Pipe[F, A, Nothing] =
    file(StandardName.FareAttributes)

  def fareRules[R](implicit encoder: Encoder[R]): Pipe[F, R, Nothing] =
    file(StandardName.FareRules)

  def shapes[S](implicit encoder: Encoder[S]): Pipe[F, S, Nothing] =
    file(StandardName.Shapes)

  def frequencies[S](implicit encoder: Encoder[S]): Pipe[F, S, Nothing] =
    file(StandardName.Frequencies)

  def transfers[T](implicit encoder: Encoder[T]): Pipe[F, T, Nothing] =
    file(StandardName.Transfers)

  def pathways[P](implicit encoder: Encoder[P]): Pipe[F, P, Nothing] =
    file(StandardName.Pathways)

  def levels[L](implicit encoder: Encoder[L]): Pipe[F, L, Nothing] =
    file(StandardName.Levels)

  def feedInfo[I](implicit encoder: Encoder[I]): Pipe[F, I, Nothing] =
    file(StandardName.FeedInfo)

  def translations[T](implicit encoder: Encoder[T]): Pipe[F, T, Nothing] =
    file(StandardName.Translations)

  def attributions[A](implicit encoder: Encoder[A]): Pipe[F, A, Nothing] =
    file(StandardName.Attributions)

}
