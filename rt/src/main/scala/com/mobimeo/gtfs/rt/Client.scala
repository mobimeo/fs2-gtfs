package com.mobimeo.gtfs
package rt

import cats.effect.*
import fs2.Stream
import fs2.io.file.{Files, Path}
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.*
import sttp.model.*

object Client {
  def download(uri: Uri) =
    for
      backend  <- HttpClientFs2Backend.resource[IO]()
      tempFile <- newTempFile
      _        <- Resource.eval(
                    basicRequest
                      .get(uri)
                      .response(asStreamAlways(Fs2Streams[IO])(writeToTempFile(tempFile)))
                      .send(backend))
    yield tempFile

  private def writeToTempFile(path: Path)(s: Stream[IO, Byte]) =
    s.through(Files[IO].writeAll(path)).compile.count

  private val newTempFile = Resource.make(IO(Path.apply("./" + System.currentTimeMillis + ".zip"))) { Files[IO].delete }
}
