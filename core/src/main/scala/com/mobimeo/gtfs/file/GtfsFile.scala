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
class GtfsFile[F[_]] private (val file: Path, fs: FileSystem)(implicit F: Sync[F], files: Files[F]) extends Gtfs[F] {
  self =>

  /** Whether the GTFS file contains the given file name. */
  def hasFile(name: String): F[Boolean] =
    files.exists(fs.getPath(s"/$name"))

  object read extends GtfsRead[F] {

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

  }

  object write extends GtfsWrite[F] {

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
