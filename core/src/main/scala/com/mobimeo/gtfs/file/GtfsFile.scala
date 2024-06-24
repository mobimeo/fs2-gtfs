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

/** Represents a GTFS file to access the content of the different files in it.
  *
  * Use the smart constructor in the companion object to acquire a `Resource` over a GTFS file.
  * The file will be closed once the resource is released.
  */
class GtfsFile[F[_]: Sync: Files] private (val provider: String, val file: Path, getPath: String => JPath) {
  type CsvRowStringDecoder[T] = CsvRowDecoder[T, String]
  type Transformer[T] = T => T

  private val files = Files[F]

  object has {
    def agency: F[Boolean] = exists(StandardName.Agency)
    def attributions: F[Boolean] = exists(StandardName.Attributions)
    def calendar: F[Boolean] = exists(StandardName.Calendar)
    def calendarDates: F[Boolean] = exists(StandardName.CalendarDates)
    def fareAttributes: F[Boolean] = exists(StandardName.FareAttributes)
    def fareRules: F[Boolean] = exists(StandardName.FareRules)
    def feedInfo: F[Boolean] = exists(StandardName.FeedInfo)
    def frequencies: F[Boolean] = exists(StandardName.Frequencies)
    def levels: F[Boolean] = exists(StandardName.Levels)
    def pathways: F[Boolean] = exists(StandardName.Pathways)
    def routes: F[Boolean] = exists(StandardName.Routes)
    def shapes: F[Boolean] = exists(StandardName.Shapes)
    def stopTimes: F[Boolean] = exists(StandardName.StopTimes)
    def stops: F[Boolean] = exists(StandardName.Stops)
    def transfers: F[Boolean] = exists(StandardName.Transfers)
    def translations: F[Boolean] = exists(StandardName.Translations)
    def trips: F[Boolean] = exists(StandardName.Trips)

    private def exists(name: StandardName): F[Boolean] = files.exists(Path.fromNioPath(getPath(s"/${name.entryName}")))
  }

  object delete {
    def agency: F[Unit] = file(StandardName.Agency)
    def stops: F[Unit] = file(StandardName.Stops)
    def routes: F[Unit] = file(StandardName.Routes)
    def trips: F[Unit] = file(StandardName.Trips)
    def stopTimes: F[Unit] = file(StandardName.StopTimes)
    def calendar: F[Unit] = file(StandardName.Calendar)
    def calendarDates: F[Unit] = file(StandardName.CalendarDates)
    def fareAttributes: F[Unit] = file(StandardName.FareAttributes)
    def fareRules: F[Unit] = file(StandardName.FareRules)
    def shapes: F[Unit] = file(StandardName.Shapes)
    def frequencies: F[Unit] = file(StandardName.Frequencies)
    def transfers: F[Unit] = file(StandardName.Transfers)
    def pathways: F[Unit] = file(StandardName.Pathways)
    def levels: F[Unit] = file(StandardName.Levels)
    def feedInfo: F[Unit] = file(StandardName.FeedInfo)
    def translations: F[Unit] = file(StandardName.Translations)
    def attributions: F[Unit] = file(StandardName.Attributions)

    private def file(name: StandardName) = files.deleteIfExists(Path.fromNioPath(getPath(s"/${name.entryName}"))).void
  }

  object read {
    /** Gives access to the raw content of CSV file `name`.
      *
      * For instance `rawFile("calendar.txt")`.
      */
    def rawFile(name: String): Stream[F, CsvRow[String]] =
      Stream.force(hasFile(name).map { exists =>
        if (exists)
          files
            .readAll(Path.fromNioPath(getPath(s"/$name")), 1024, Flags.Read)
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

  object write {
    /** Gives access to the pipe to save in file `name`.
      *
      * For instance `rawFile("agency.txt")`.
      */
    def rawFile(name: String): Pipe[F, CsvRow[String], Nothing] =
      s =>
        Stream
          .resource(files.tempFile)
          .flatMap { tempFile =>
            // save the rows in the temp file first
            s.through(encodeRowWithFirstHeaders)
              .through(toRowStrings())
              .through(text.utf8.encode)
              .through(files.writeAll(tempFile)) ++
              // once temp file is saved, copy it to the destination file in GTFS
              Stream.exec(
                files.copy(tempFile, Path.fromNioPath(getPath(s"/$name")), CopyFlags(CopyFlag.ReplaceExisting)).void
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

    private def file[T](name: StandardName)(using CsvRowEncoder[T, String]): Pipe[F, T, Nothing] =
      _.through(encodeRow).through(rawFile(name))

    private def rawFile(name: StandardName): Pipe[F, CsvRow[String], Nothing] =
      s =>
        Stream
          .resource(files.tempFile)
          .flatMap { tempFile =>
            // save the rows in the temp file first
            s.through(encodeRowWithFirstHeaders)
              .through(toRowStrings())
              .through(text.utf8.encode)
              .through(files.writeAll(tempFile)) ++
              // once temp file is saved, copy it to the destination file in GTFS
              Stream.exec(
                files.copy(tempFile, Path.fromNioPath(getPath(s"/${name.entryName}")), CopyFlags(CopyFlag.ReplaceExisting)).void
              )
          }
  }

  /** Creates a GTFS target which originally consists of the content of this one.
    *
    * The file is copied when the resource is acquired.
    * This can be used when the result of transforming this GTFS file content is toRowStrings to be saved to a new file.
    */
  def copyTo(file: Path, flags: CopyFlags = CopyFlags.empty): Resource[F, GtfsFile[F]] =
    Resource.eval(files.copy(this.file, file, flags)) >> GtfsFile(provider, file)
}

object GtfsFile {
  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def apply[F[_]: Sync: Files](provider: String, file: Path, create: Boolean = false): Resource[F, GtfsFile[F]] =
    def getFileSystem(uri: URI, env: Map[String, String]) = Sync[F].blocking {
      try FileSystems.newFileSystem(uri, env.asJava)
      catch case _: FileSystemAlreadyExistsException => FileSystems.getFileSystem(uri)
    }
    for
      exists <- Resource.eval(Files[F].exists(file))
      uri     = URI.create("jar:file:" + file.absolute)
      env     = Map("create" -> String.valueOf(create && !exists))
      acquire = getFileSystem(uri, env)
      fs     <- Resource.make(acquire) { fs => Sync[F].blocking(fs.close()) }
    yield new GtfsFile(provider, file, fs.getPath(_))

  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def fromClasspath[F[_]: Sync: Files](provider: String, resource: URL): Resource[F, GtfsFile[F]] =
    apply(provider, Path.fromNioPath(JPath.of(resource.toURI)))
}
