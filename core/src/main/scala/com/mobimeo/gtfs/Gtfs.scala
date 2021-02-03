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
import cats.implicits._

import fs2._
import fs2.data.csv._

import java.nio.file.{CopyOption, FileSystem, FileSystems, Files, Path, Paths, StandardCopyOption}

import scala.jdk.CollectionConverters._

import scala.util.Properties
import java.net.URI

/** Represents a GTFS file. Can be used to access the content of the different
  * files in it.
  *
  * Use the smart constructor in the companion object to acquire a `Resource`
  * over a GTFS file. The file will be closed once the resource is released.
  */
class Gtfs[F[_]] private (val file: Path, fs: FileSystem, blocker: Blocker)(implicit F: Sync[F], cs: ContextShift[F]) {
  self =>

  /** Namespace containing operators and pipes to read content of a GTFS file access
    * stream.
    */
  object read {

    /** Gives access to the raw content of CSV file `name`.
      *
    * For instance `rawFile("calendar.txt")`.
      */
    def rawFile(name: String): Stream[F, CsvRow[String]] =
      io.file
        .readAll(fs.getPath(s"/$name"), blocker, 1024)
        .through(text.utf8Decode)
        .flatMap(Stream.emits(_))
        .through(rows[F]())
        .through(headers[F, String])

    /** Gives access to the content of CSV file `name`.
      *
    * For instance `file("calendar.txt")`.
      */
    def file[R](name: String)(implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      rawFile(name).through(decodeRow[F, String, R])

    // aliases for standard GTFS files

    def stops[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(Gtfs.Names.Stops)

    def rawStops: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Stops)

    def rawRoutes: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Routes)

    def routes[R](implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      file(Gtfs.Names.Routes)

    def rawTrips: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Trips)

    def trips[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(Gtfs.Names.Trips)

    def rawStopTimes: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.StopTimes)

    def stopTimes[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(Gtfs.Names.StopTimes)

    def rawAgencies: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Agency)

    def agencies[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(Gtfs.Names.Agency)

    def rawCalendar: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Calendar)

    def calendar[C](implicit decoder: CsvRowDecoder[C, String]): Stream[F, C] =
      file(Gtfs.Names.Calendar)

    def rawCalendarDates: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.CalendarDates)

    def calendarDates[C](implicit decoder: CsvRowDecoder[C, String]): Stream[F, C] =
      file(Gtfs.Names.CalendarDates)

    def rawFareAttributes: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.FareAttributes)

    def fareAttributes[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(Gtfs.Names.FareAttributes)

    def rawFareRules: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.FareRules)

    def fareRules[R](implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      file(Gtfs.Names.FareRules)

    def rawShapes: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Shapes)

    def shapes[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(Gtfs.Names.Shapes)

    def rawFrequencies: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Frequencies)

    def frequencies[S](implicit decoder: CsvRowDecoder[S, String]): Stream[F, S] =
      file(Gtfs.Names.Frequencies)

    def rawTransfers: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Transfers)

    def transfers[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(Gtfs.Names.Transfers)

    def rawPathways: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Pathways)

    def pathways[P](implicit decoder: CsvRowDecoder[P, String]): Stream[F, P] =
      file(Gtfs.Names.Pathways)

    def rawLevels: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Levels)

    def levels[L](implicit decoder: CsvRowDecoder[L, String]): Stream[F, L] =
      file(Gtfs.Names.Levels)

    def rawFeedInfo: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.FeedInfo)

    def feedInfo[I](implicit decoder: CsvRowDecoder[I, String]): Stream[F, I] =
      file(Gtfs.Names.FeedInfo)

    def rawTranslations: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Translations)

    def translations[T](implicit decoder: CsvRowDecoder[T, String]): Stream[F, T] =
      file(Gtfs.Names.Translations)

    def rawAttributions: Stream[F, CsvRow[String]] =
      rawFile(Gtfs.Names.Attributions)

    def attributions[A](implicit decoder: CsvRowDecoder[A, String]): Stream[F, A] =
      file(Gtfs.Names.Attributions)

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
    def rawFile(file: String): Pipe[F, CsvRow[String], Unit] =
      s =>
        io.file
          .tempFileStream(blocker, Paths.get(Properties.propOrElse("java.io.tmpdir", "/tmp")), prefix = file)
          .flatMap { tempFile =>
            // save the rows in the temp file first
            s.through(encodeRowWithFirstHeaders)
              .through(toRowStrings())
              .through(text.utf8Encode)
              .through(io.file.writeAll(tempFile, blocker)) ++
              // once temp file is saved, copy it to the destination file in GTFS
              Stream.eval_(
                io.file.copy(blocker, tempFile, fs.getPath(s"/$file"), Seq(StandardCopyOption.REPLACE_EXISTING))
              )
          }

    /** Gives access to the pipe to save in file `name`.
      *
      * For instance `file("agency.txt")`.
      */
    def file[T](file: String)(implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      _.through(encodeRow).through(rawFile(file))

    // aliases for standard GTFS files

    def stops[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(Gtfs.Names.Stops)

    def rawStops: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Stops)

    def rawRoutes: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Routes)

    def routes[R](implicit encoder: CsvRowEncoder[R, String]): Pipe[F, R, Unit] =
      file(Gtfs.Names.Routes)

    def rawTrips: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Trips)

    def trips[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(Gtfs.Names.Trips)

    def rawStopTimes: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.StopTimes)

    def stopTimes[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(Gtfs.Names.StopTimes)

    def rawAgencies: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Agency)

    def agencies[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(Gtfs.Names.Agency)

    def rawCalendar: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Calendar)

    def calendar[C](implicit encoder: CsvRowEncoder[C, String]): Pipe[F, C, Unit] =
      file(Gtfs.Names.Calendar)

    def rawCalendarDates: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.CalendarDates)

    def calendarDates[C](implicit encoder: CsvRowEncoder[C, String]): Pipe[F, C, Unit] =
      file(Gtfs.Names.CalendarDates)

    def rawFareAttributes: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.FareAttributes)

    def fareAttributes[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(Gtfs.Names.FareAttributes)

    def rawFareRules: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.FareRules)

    def fareRules[R](implicit encoder: CsvRowEncoder[R, String]): Pipe[F, R, Unit] =
      file(Gtfs.Names.FareRules)

    def rawShapes: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Shapes)

    def shapes[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(Gtfs.Names.Shapes)

    def rawFrequencies: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Frequencies)

    def frequencies[S](implicit encoder: CsvRowEncoder[S, String]): Pipe[F, S, Unit] =
      file(Gtfs.Names.Frequencies)

    def rawTransfers: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Transfers)

    def transfers[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(Gtfs.Names.Transfers)

    def rawPathways: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Pathways)

    def pathways[P](implicit encoder: CsvRowEncoder[P, String]): Pipe[F, P, Unit] =
      file(Gtfs.Names.Pathways)

    def rawLevels: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Levels)

    def levels[L](implicit encoder: CsvRowEncoder[L, String]): Pipe[F, L, Unit] =
      file(Gtfs.Names.Levels)

    def rawFeedInfo: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.FeedInfo)

    def feedInfo[I](implicit encoder: CsvRowEncoder[I, String]): Pipe[F, I, Unit] =
      file(Gtfs.Names.FeedInfo)

    def rawTranslations: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Translations)

    def translations[T](implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Unit] =
      file(Gtfs.Names.Translations)

    def rawAttributions: Pipe[F, CsvRow[String], Unit] =
      rawFile(Gtfs.Names.Attributions)

    def attributions[A](implicit encoder: CsvRowEncoder[A, String]): Pipe[F, A, Unit] =
      file(Gtfs.Names.Attributions)

  }

  /** Creates a GTFS target which originally consists of the content of this one.
    * The file is copied when the resource is acquired.
    *
    * This can be used when the result of transforming this GTFS file content is toRowStrings
    * to be saved to a new file.
    */
  def copyTo(file: Path, flags: Seq[CopyOption] = Seq.empty): Resource[F, Gtfs[F]] =
    Resource.liftF(fs2.io.file.copy(blocker, self.file, file, flags)) >> Gtfs(file, blocker)

}

object Gtfs {

  private[gtfs] def makeFs[F[_]](
      file: Path,
      blocker: Blocker,
      create: Boolean
  )(implicit F: Sync[F], cs: ContextShift[F]): Resource[F, FileSystem] =
    Resource.make(
      blocker.delay(
        FileSystems
          .newFileSystem(
            URI.create("jar:file:" + file.toAbsolutePath()),
            Map("create" -> String.valueOf(create && Files.notExists(file))).asJava
          )
      )
    )(fs => blocker.delay(fs.close()))

  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def apply[F[_]](file: Path, blocker: Blocker, create: Boolean = false)(implicit
      F: Sync[F],
      cs: ContextShift[F]
  ): Resource[F, Gtfs[F]] =
    makeFs(file, blocker, create).map(new Gtfs(file, _, blocker))

  /** Standard GTFS file names. */
  object Names {
    val Stops          = "stops.txt"
    val Routes         = "routes.txt"
    val Trips          = "trips.txt"
    val StopTimes      = "stop_times.txt"
    val Agency         = "agency.txt"
    val Calendar       = "calendar.txt"
    val CalendarDates  = "calendar_dates.txt"
    val FareAttributes = "fare_attributes.txt"
    val FareRules      = "fare_rules.txt"
    val Shapes         = "shapes.txt"
    val Frequencies    = "frequencies.txt"
    val Transfers      = "transfers.txt"
    val Pathways       = "pathways.txt"
    val Levels         = "levels.txt"
    val FeedInfo       = "feed_info.txt"
    val Translations   = "translations.txt"
    val Attributions   = "attributions.txt"
  }

}
