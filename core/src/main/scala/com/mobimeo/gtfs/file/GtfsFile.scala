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
package file

import cats.effect.*
import cats.syntax.all.*
import com.mobimeo.gtfs.*
import fs2.*
import fs2.data.csv.*
import fs2.data.csv.lowlevel.*
import fs2.io.file.*
import java.net.*
import java.nio.file.{FileSystems, FileSystemAlreadyExistsException, Path => JPath}
import model.*
import scala.jdk.CollectionConverters.*

import com.mobimeo.gtfs._

import fs2._
import fs2.io.file._
import fs2.data.csv._
import fs2.data.csv.lowlevel._

import scala.jdk.CollectionConverters._

import java.nio.file.{FileSystem, FileSystems}

import java.net.URI
import com.mobimeo.gtfs.StandardName

/** Represents a GTFS file. Can be used to access the content of the different files in it.
  *
  * Use the smart constructor in the companion object to acquire a `Resource` over a GTFS file. The file will be closed
  * once the resource is released.
  */
class GtfsFile[F[_]] private (val file: Path, fs: FileSystem)(implicit F: Sync[F], files: Files[F])
    extends Gtfs[F, CsvRowDecoder[*, String], CsvRowEncoder[*, String]] {

  object has extends GtfsHas[F] {
    def file(name: String): F[Boolean] =
      files.exists(Path.fromNioPath(fs.getPath(s"/$name")))
  }

  object delete extends GtfsDelete[F] {
    def file(name: String): F[Unit] =
      files.deleteIfExists(Path.fromNioPath(fs.getPath(s"/$name"))).void
  }

  object read extends GtfsRead[F, CsvRowDecoder[*, String]] {

    /** Gives access to the raw content of CSV file `name`.
      *
      * For instance `rawFile("calendar.txt")`.
      */
    def rawFile(name: String): Stream[F, CsvRow[String]] =
      Stream.force(has.file(name).map { exists =>
        if (exists)
          files
            .readAll(Path.fromNioPath(fs.getPath(s"/$name")), 1024, Flags.Read)
            .through(text.utf8.decode)
            .through(rows())
            .through(headers[F, String])
        else
          Stream.empty
      })

    def agencies:       Stream[F, Agency]         = readFile(StandardName.Agency, noAction)
    def attributions:   Stream[F, Attribution]    = readFile(StandardName.Attributions, noAction)
    def calendar:       Stream[F, Calendar]       = readFile(StandardName.Calendar, noAction)
    def calendarDates:  Stream[F, CalendarDate]   = readFile(StandardName.CalendarDates, noAction)
    def fareAttributes: Stream[F, FareAttribute]  = readFile(StandardName.FareAttributes, noAction)
    def fareRules:      Stream[F, FareRules]      = readFile(StandardName.FareRules, noAction)
    def feedInfo:       Stream[F, FeedInfo]       = readFile(StandardName.FeedInfo, noAction)
    def frequencies:    Stream[F, Frequency]      = readFile(StandardName.Frequencies, noAction)
    def levels:         Stream[F, Level]          = readFile(StandardName.Levels, noAction)
    def pathways:       Stream[F, Pathway]        = readFile(StandardName.Pathways, noAction)
    def routes:         Stream[F, Route]          = readFile(StandardName.Routes, noAction)
    def shapes:         Stream[F, Shape]          = readFile(StandardName.Shapes, noAction)
    def stopTimes:      Stream[F, StopTime]       = readFile(StandardName.StopTimes, noAction)
    def stops:          Stream[F, Stop]           = readFile(StandardName.Stops, noAction)
    def transfers:      Stream[F, Transfer]       = readFile(StandardName.Transfers, noAction)
    def translations:   Stream[F, Translation]    = readFile(StandardName.Translations, noAction)
    def trips:          Stream[F, Trip]           = readFile(StandardName.Trips, noAction)

    private def readFile[T: CsvRowStringDecoder](name: StandardName, transformer: Transformer[T]) =
      Stream
        .force(hasFile(name.entryName).map { exists =>
                 if (exists)
                   files
                     .readAll(Path.fromNioPath(getPath(s"/${name.entryName}")), 1024, Flags.Read)
                     .through(text.utf8.decode)
                     .through(rows())
                     .through(headers[F, String])
                 else Stream.empty
               })
        .through { decodeRow }
        .through { _.map(transformer) }

    private def hasFile(name: String): F[Boolean] = files.exists(Path.fromNioPath(getPath(s"/$name")))

    private def noAction[T] = identity[T]
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
            files.tempFile
          )
          .flatMap { tempFile =>
            // save the rows in the temp file first
            s.through(encodeRowWithFirstHeaders)
              .through(toRowStrings())
              .through(text.utf8.encode)
              .through(files.writeAll(tempFile)) ++
              // once temp file is saved, copy it to the destination file in GTFS
              Stream.exec(
                files.copy(tempFile, Path.fromNioPath(fs.getPath(s"/$name")), CopyFlags(CopyFlag.ReplaceExisting)).void
              )
          }

    def agencies: Pipe[F, Agency, Nothing]              = file(StandardName.Agency)
    def attributions: Pipe[F, Attribution, Nothing]     = file(StandardName.Attributions)
    def calendar: Pipe[F, Calendar, Nothing]            = file(StandardName.Calendar)
    def calendarDates: Pipe[F, CalendarDate, Nothing]   = file(StandardName.CalendarDates)
    def fareAttributes: Pipe[F, FareAttribute, Nothing] = file(StandardName.FareAttributes)
    def fareRules: Pipe[F, FareRules, Nothing]          = file(StandardName.FareRules)
    def feedInfo: Pipe[F, FeedInfo, Nothing]            = file(StandardName.FeedInfo)
    def frequencies: Pipe[F, Frequency, Nothing]        = file(StandardName.Frequencies)
    def levels: Pipe[F, Level, Nothing]                 = file(StandardName.Levels)
    def pathways: Pipe[F, Pathway, Nothing]             = file(StandardName.Pathways)
    def routes: Pipe[F, Route, Nothing]                 = file(StandardName.Routes)
    def shapes: Pipe[F, Shape, Nothing]                 = file(StandardName.Shapes)
    def stopTimes: Pipe[F, StopTime, Nothing]           = file(StandardName.StopTimes)
    def stops: Pipe[F, Stop, Nothing]                   = file(StandardName.Stops)
    def transfers: Pipe[F, Transfer, Nothing]           = file(StandardName.Transfers)
    def translations: Pipe[F, Translation, Nothing]     = file(StandardName.Translations)
    def trips: Pipe[F, Trip, Nothing]                   = file(StandardName.Trips)

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

  /** Creates a GTFS target which originally consists of the content of this one. The file is copied when the resource
    * is acquired.
    *
    * This can be used when the result of transforming this GTFS file content is toRowStrings to be saved to a new file.
    */
  def copyTo(file: Path, flags: CopyFlags = CopyFlags.empty): Resource[F, GtfsFile[F]] =
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
              URI.create("jar:file:" + file.absolute),
              Map("create" -> String.valueOf(create && !exists)).asJava
            )
        )
      }
    )(fs => F.blocking(fs.close()))

  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def apply[F[_]](file: Path, create: Boolean = false)(implicit F: Sync[F], files: Files[F]): Resource[F, GtfsFile[F]] =
    makeFs(file, create).map(new GtfsFile(file, _))

}
