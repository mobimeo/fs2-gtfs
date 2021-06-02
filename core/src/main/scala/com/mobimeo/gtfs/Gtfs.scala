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

import cats.effect._
import cats.syntax.all._

import fs2._
import fs2.io.file.Files
import fs2.data.csv._
import fs2.data.csv.lowlevel._

import java.nio.file.{CopyOption, FileSystem, FileSystems, Path, Paths, StandardCopyOption}

import scala.jdk.CollectionConverters._

import scala.util.Properties
import java.net.URI

/** Represents a GTFS file. Can be used to access the content of the different
  * files in it.
  *
  * Use the smart constructor in the companion object to acquire a `Resource`
  * over a GTFS file. The file will be closed once the resource is released.
  */
class Gtfs[F[_]] private (val file: Path, fs: FileSystem)(implicit F: Sync[F], files: Files[F]) {
  self =>

  /** Whether the GTFS file contains the given file name. */
  def hasFile(name: String): F[Boolean] =
    files.exists(fs.getPath(s"/$name"))

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
  object read {

    /** Gives access to the raw content of CSV file `name`.
      *
    * For instance `rawFile("calendar.txt")`.
      */
    def rawFile(name: String): Stream[F, CsvRow[String]] =
      Stream.force(hasFile(name).map { exists =>
        if (exists)
          files
            .readAll(fs.getPath(s"/$name"), 1024)
            .through(text.utf8Decode)
            .through(rows())
            .through(headers[F, String])
        else
          Stream.empty
      })

    /** Gives access to the raw content of CSV file `name`. */
    def rawFile(name: StandardName): Stream[F, CsvRow[String]] =
      rawFile(name.entryName)

    /** Gives access to the content of CSV file `name`.
      *
    * For instance `file("calendar.txt")`.
      */
    def file[R](name: String)(implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      rawFile(name).through(decodeRow)

    /** Gives access to the content of CSV file `name`. */
    def file[R](name: StandardName)(implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      file(name.entryName)

    // aliases for standard GTFS files

    def stops[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(StandardName.Stops)

    def rawStops: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Stops)

    def rawRoutes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Routes)

    def routes[R](implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      file(StandardName.Routes)

    def rawTrips: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Trips)

    def trips[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(StandardName.Trips)

    def rawStopTimes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.StopTimes)

    def stopTimes[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(StandardName.StopTimes)

    def rawAgencies: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Agency)

    def agencies[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(StandardName.Agency)

    def rawCalendar: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Calendar)

    def calendar[C](implicit decoder: CsvRowDecoder[C, String]): Stream[F, C] =
      file(StandardName.Calendar)

    def rawCalendarDates: Stream[F, CsvRow[String]] =
      rawFile(StandardName.CalendarDates)

    def calendarDates[C](implicit decoder: CsvRowDecoder[C, String]): Stream[F, C] =
      file(StandardName.CalendarDates)

    def rawFareAttributes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FareAttributes)

    def fareAttributes[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(StandardName.FareAttributes)

    def rawFareRules: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FareRules)

    def fareRules[R](implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      file(StandardName.FareRules)

    def rawShapes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Shapes)

    def shapes[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(StandardName.Shapes)

    def rawFrequencies: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Frequencies)

    def frequencies[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(StandardName.Frequencies)

    def rawTransfers: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Transfers)

    def transfers[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(StandardName.Transfers)

    def rawPathways: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Pathways)

    def pathways[P](implicit decoder: CsvRowDecoder[P, String]): Stream[F, P] =
      file(StandardName.Pathways)

    def rawLevels: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Levels)

    def levels[L](implicit decoder: CsvRowDecoder[L, String]): Stream[F, L] =
      file(StandardName.Levels)

    def rawFeedInfo: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FeedInfo)

    def feedInfo[I](implicit decoder: CsvRowDecoder[I, String]): Stream[F, I] =
      file(StandardName.FeedInfo)

    def rawTranslations: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Translations)

    def translations[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(StandardName.Translations)

    def rawAttributions: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Attributions)

    def attributions[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(StandardName.Attributions)

  }

  /** Namespace containing operators and pipes to save the Result of a GTFS stream.
    * This can be used to save the result of transformations.
    *
    * Once saved, the content of the GTFS file is modified, subsequent accesses
    * to the same file in the same GTFS file will contain modications.
    */
  object write {

    /** Gives access to the pipe to save in file `name`.
      *
      * For instance `rawFile("agency.txt")`.
      */
    def rawFile(name: String): Pipe[F, CsvRow[String], Unit] =
      s =>
        Stream
          .resource(
            files
              .tempFile(Paths.get(Properties.propOrElse("java.io.tmpdir", "/tmp")).some, prefix = name)
          )
          .flatMap { tempFile =>
            // save the rows in the temp file first
            s.through(encodeRowWithFirstHeaders)
              .through(toRowStrings())
              .through(text.utf8Encode)
              .through(files.writeAll(tempFile)) ++
              // once temp file is saved, copy it to the destination file in GTFS
              Stream.exec(
                files.copy(tempFile, fs.getPath(s"/$name"), Seq(StandardCopyOption.REPLACE_EXISTING)).void
              )
          }

    /** Gives access to the pipe to save in file `name`. */
    def rawFile(name: StandardName): Pipe[F, CsvRow[String], Unit] =
      rawFile(name.entryName)

    /** Gives access to the pipe to save in file `name`.
      *
      * For instance `file("agency.txt")`.
      */
    def file[T](name: String)(implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      _.through(encodeRow).through(rawFile(name))

    /** Gives access to the pipe to save in file `name`. */
    def file[T](name: StandardName)(implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(name.entryName)

    // aliases for standard GTFS files

    def stops[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(StandardName.Stops)

    def rawStops: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Stops)

    def rawRoutes: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Routes)

    def routes[R](implicit encoder: CsvRowEncoder[R, String]): Pipe[F, R, Unit] =
      file(StandardName.Routes)

    def rawTrips: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Trips)

    def trips[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(StandardName.Trips)

    def rawStopTimes: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.StopTimes)

    def stopTimes[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(StandardName.StopTimes)

    def rawAgencies: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Agency)

    def agencies[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(StandardName.Agency)

    def rawCalendar: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Calendar)

    def calendar[C](implicit encoder: CsvRowEncoder[C, String]): Pipe[F, C, Unit] =
      file(StandardName.Calendar)

    def rawCalendarDates: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.CalendarDates)

    def calendarDates[C](implicit encoder: CsvRowEncoder[C, String]): Pipe[F, C, Unit] =
      file(StandardName.CalendarDates)

    def rawFareAttributes: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.FareAttributes)

    def fareAttributes[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(StandardName.FareAttributes)

    def rawFareRules: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.FareRules)

    def fareRules[R](implicit encoder: CsvRowEncoder[R, String]): Pipe[F, R, Unit] =
      file(StandardName.FareRules)

    def rawShapes: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Shapes)

    def shapes[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(StandardName.Shapes)

    def rawFrequencies: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Frequencies)

    def frequencies[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(StandardName.Frequencies)

    def rawTransfers: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Transfers)

    def transfers[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(StandardName.Transfers)

    def rawPathways: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Pathways)

    def pathways[P](implicit encoder: CsvRowEncoder[P, String]): Pipe[F, P, Unit] =
      file(StandardName.Pathways)

    def rawLevels: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Levels)

    def levels[L](implicit encoder: CsvRowEncoder[L, String]): Pipe[F, L, Unit] =
      file(StandardName.Levels)

    def rawFeedInfo: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.FeedInfo)

    def feedInfo[I](implicit encoder: CsvRowEncoder[I, String]): Pipe[F, I, Unit] =
      file(StandardName.FeedInfo)

    def rawTranslations: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Translations)

    def translations[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(StandardName.Translations)

    def rawAttributions: Pipe[F, CsvRow[String], Unit] =
      rawFile(StandardName.Attributions)

    def attributions[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(StandardName.Attributions)

  }

  /** Creates a GTFS target which originally consists of the content of this one.
    * The file is copied when the resource is acquired.
    *
    * This can be used when the result of transforming this GTFS file content is toRowStrings
    * to be saved to a new file.
    */
  def copyTo(file: Path, flags: Seq[CopyOption] = Seq.empty): Resource[F, Gtfs[F]] =
    Resource.eval(files.copy(self.file, file, flags)) >> Gtfs(file)

}

object Gtfs {

  private[gtfs] def makeFs[F[_]](
      file: Path,
      create: Boolean
  )(implicit F: Sync[F], files: Files[F]): Resource[F, FileSystem] =
    Resource.make(
      files.exists(file).flatMap { exists =>
        F.blocking(
          FileSystems
            .newFileSystem(
              URI.create("jar:file:" + file.toAbsolutePath()),
              Map("create" -> String.valueOf(create && !exists)).asJava
            )
        )
      }
    )(fs => F.blocking(fs.close()))

  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def apply[F[_]](file: Path, create: Boolean = false)(implicit F: Sync[F], files: Files[F]): Resource[F, Gtfs[F]] =
    makeFs(file, create).map(new Gtfs(file, _))

}
