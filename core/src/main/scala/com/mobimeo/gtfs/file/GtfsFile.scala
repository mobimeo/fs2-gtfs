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

import cats.effect.*
import cats.syntax.all.*
import com.mobimeo.gtfs.*
import fs2.*
import fs2.data.csv.*
import fs2.data.csv.lowlevel.*
import fs2.io.file.*
import java.net.*
import java.nio.file.{FileSystems, Path => JPath}
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Represents a GTFS file to access the content of the different files in it.
  *
  * Use the smart constructor in the companion object to acquire a `Resource` over a GTFS file.
  * The file will be closed once the resource is released.
  */
class GtfsFile[F[_]: Sync: Files] private (val tenant: String, val file: Path, getPath: String => JPath)
    extends Gtfs[F, CsvRowDecoder[*, String], CsvRowEncoder[*, String]] {
  self =>
  private val files = Files[F]

  object has extends GtfsHas[F] {
    def file(name: String): F[Boolean] = files.exists(Path.fromNioPath(getPath(s"/$name")))
  }

  object delete extends GtfsDelete[F] {
    def file(name: String): F[Unit] = files.deleteIfExists(Path.fromNioPath(getPath(s"/$name"))).void
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
            .readAll(Path.fromNioPath(getPath(s"/$name")), 1024, Flags.Read)
            .through(text.utf8.decode)
            .through(rows())
            .through(headers[F, String])
        else
          Stream.empty
      })

    /** Gives access to the raw content of CSV file `name`. */
    def rawFile(name: StandardName): Stream[F, CsvRow[String]] = rawFile(name.entryName)
    def file[R](name: String)(implicit decoder: CsvRowDecoder[R, String]): Stream[F, R] = rawFile(name).through(decodeRow)
    def rawStops: Stream[F, CsvRow[String]] = rawFile(StandardName.Stops)
    def rawRoutes: Stream[F, CsvRow[String]] = rawFile(StandardName.Routes)
    def rawTrips: Stream[F, CsvRow[String]] = rawFile(StandardName.Trips)
    def rawStopTimes: Stream[F, CsvRow[String]] = rawFile(StandardName.StopTimes)
    def rawAgencies: Stream[F, CsvRow[String]] = rawFile(StandardName.Agency)
    def rawCalendar: Stream[F, CsvRow[String]] = rawFile(StandardName.Calendar)
    def rawCalendarDates: Stream[F, CsvRow[String]] = rawFile(StandardName.CalendarDates)
    def rawFareAttributes: Stream[F, CsvRow[String]] = rawFile(StandardName.FareAttributes)
    def rawFareRules: Stream[F, CsvRow[String]] = rawFile(StandardName.FareRules)
    def rawShapes: Stream[F, CsvRow[String]] = rawFile(StandardName.Shapes)
    def rawFrequencies: Stream[F, CsvRow[String]] = rawFile(StandardName.Frequencies)
    def rawTransfers: Stream[F, CsvRow[String]] = rawFile(StandardName.Transfers)
    def rawPathways: Stream[F, CsvRow[String]] = rawFile(StandardName.Pathways)
    def rawLevels: Stream[F, CsvRow[String]] = rawFile(StandardName.Levels)
    def rawFeedInfo: Stream[F, CsvRow[String]] = rawFile(StandardName.FeedInfo)
    def rawTranslations: Stream[F, CsvRow[String]] = rawFile(StandardName.Translations)
    def rawAttributions: Stream[F, CsvRow[String]] = rawFile(StandardName.Attributions)
  }

  object write extends GtfsWrite[F, CsvRowEncoder[*, String]] {
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

    /** Gives access to the pipe to save in file `name`. */
    def rawFile(name: StandardName): Pipe[F, CsvRow[String], Nothing] = rawFile(name.entryName)
    def file[T](name: String)(implicit encoder: CsvRowEncoder[T, String]): Pipe[F, T, Nothing] =
      _.through(encodeRow).through(rawFile(name))
    def rawStops: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Stops)
    def rawShapes: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Shapes)
    def rawFrequencies: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Frequencies)
    def rawTransfers: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Transfers)
    def rawPathways: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Pathways)
    def rawLevels: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Levels)
    def rawFeedInfo: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.FeedInfo)
    def rawTranslations: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Translations)
    def rawAttributions: Pipe[F, CsvRow[String], Nothing] = rawFile(StandardName.Attributions)
  }

  /** Creates a GTFS target which originally consists of the content of this one.
    *
    * The file is copied when the resource is acquired.
    * This can be used when the result of transforming this GTFS file content is toRowStrings to be saved to a new file.
    */
  def copyTo(file: Path, flags: CopyFlags = CopyFlags.empty): Resource[F, GtfsFile[F]] =
    Resource.eval(files.copy(self.file, file, flags)) >> GtfsFile(tenant, file)
}

object GtfsFile {
  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def apply[F[_]: Sync: Files](tenant: String, file: Path, create: Boolean = false): Resource[F, GtfsFile[F]] =
    for
      exists <- Resource.eval(Files[F].exists(file))
      uri     = URI.create("jar:file:" + file.absolute)
      env     = Map("create" -> String.valueOf(create && !exists)).asJava
      acquire = Sync[F].blocking(Try(FileSystems.newFileSystem(uri, env)).get)
      fs     <- Resource.make(acquire) { fs => Sync[F].blocking(fs.close()) }
    yield new GtfsFile(tenant, file, fs.getPath(_))

  /** Creates a GTFS object, giving access to all files within the GTFS file. */
  def fromClasspath[F[_]: Sync: Files](tenant: String, resource: URL): Resource[F, GtfsFile[F]] =
    apply(tenant, Path.fromNioPath(JPath.of(resource.toURI)))
}
