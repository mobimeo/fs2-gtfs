package com.mobimeo.gtfs.file

import cats.effect.*
import cats.implicits.*
import fs2.io.file.*
import java.nio.file.{Path => JPath}
import weaver.*
import com.mobimeo.gtfs.model.Agency
import java.time.ZoneId

object GtfsFileSpec extends IOSuite {
  override type Res = GtfsFile[IO]
  override def sharedResource: Resource[IO, Res] =
    GtfsFile(Path.fromNioPath(JPath.of(getClass.getResource("simple-gtfs.zip").toURI)))

  test("agencies can be read") { gtfsFile =>
    for
      agencies <- gtfsFile.read.agencies[Agency].compile.toList
    yield
      List(
        expect(agencies.size == 3),
        expect(agencies(0).name == "An Agency"),
        expect(agencies(1).id == "B"),
        expect(agencies(2).timezone == ZoneId.of("Europe/Berlin")),
      ).combineAll
  }
}
