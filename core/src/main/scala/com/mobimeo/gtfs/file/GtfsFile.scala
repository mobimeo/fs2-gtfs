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

package com.mobimeo.gtfs.file

import cats.effect._
import cats.syntax.all._

import com.mobimeo.gtfs._

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
class GtfsFile[F[_]] private (val file: Path, fs: FileSystem)(implicit F: Sync[F], files: Files[F])
    extends Gtfs[F, CsvRowDecoder[*, String], CsvRowEncoder[*, String]] {
  self =>

  /** Whether the GTFS file contains the given file name. */
  def hasFile(name: String): F[Boolean] =
    files.exists(fs.getPath(s"/$name"))

  object read extends GtfsRead[F, CsvRowDecoder[*, String]] {

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

    def file[R](name: String)(implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] =
      rawFile(name).through(decodeRow)

    def rawStops: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Stops)

    def rawRoutes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Routes)

    def rawTrips: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Trips)

    def rawStopTimes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.StopTimes)

    def rawAgencies: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Agency)

    def rawCalendar: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Calendar)

    def rawCalendarDates: Stream[F, CsvRow[String]] =
      rawFile(StandardName.CalendarDates)

    def rawFareAttributes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FareAttributes)

    def rawFareRules: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FareRules)

    def rawShapes: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Shapes)

    def rawFrequencies: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Frequencies)

    def rawTransfers: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Transfers)

    def rawPathways: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Pathways)

    def rawLevels: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Levels)

    def rawFeedInfo: Stream[F, CsvRow[String]] =
      rawFile(StandardName.FeedInfo)

    def rawTranslations: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Translations)

    def rawAttributions: Stream[F, CsvRow[String]] =
      rawFile(StandardName.Attributions)

  }

  object write extends GtfsWrite[F, CsvRowEncoder[*, String]] {

    /** Gives access to the pipe to save in file `name`.
      *
      * For instance `rawFile("agency.txt")`.
      */
    def rawFile(name: String): Pipe[F, CsvRow[String], Nothing] =
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
    def rawFile(name: StandardName): Pipe[F, CsvRow[String], Nothing] =
      rawFile(name.entryName)

    def file[T](name: String)(implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Nothing] =
      _.through(encodeRow).through(rawFile(name))

    def rawStops: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Stops)

    def rawRoutes: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Routes)

    def rawTrips: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Trips)

    def rawStopTimes: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.StopTimes)

    def rawAgencies: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Agency)

    def rawCalendar: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Calendar)

    def rawCalendarDates: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.CalendarDates)

    def rawFareAttributes: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.FareAttributes)

    def rawFareRules: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.FareRules)

    def rawShapes: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Shapes)

    def rawFrequencies: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Frequencies)

    def rawTransfers: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Transfers)

    def rawPathways: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Pathways)

    def rawLevels: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Levels)

    def rawFeedInfo: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.FeedInfo)

    def rawTranslations: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Translations)

    def rawAttributions: Pipe[F, CsvRow[String], Nothing] =
      rawFile(StandardName.Attributions)

  }

  /** Creates a GTFS target which originally consists of the content of this one.
    * The file is copied when the resource is acquired.
    *
    * This can be used when the result of transforming this GTFS file content is toRowStrings
    * to be saved to a new file.
    */
  def copyTo(file: Path, flags: Seq[CopyOption] = Seq.empty): Resource[F, GtfsFile[F]] =
    Resource.eval(files.copy(self.file, file, flags)) >> GtfsFile(file)

}

object GtfsFile {

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
  def apply[F[_]](file: Path, create: Boolean = false)(implicit F: Sync[F], files: Files[F]): Resource[F, GtfsFile[F]] =
    makeFs(file, create).map(new GtfsFile(file, _))

}
