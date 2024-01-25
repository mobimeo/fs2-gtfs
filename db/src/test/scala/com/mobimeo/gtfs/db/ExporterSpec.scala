package com.mobimeo.gtfs.db

import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import fs2.io.file.Path
import weaver.*

object ExporterSpec extends PostgresSuite {
  test("exports from database to GTFS file") { xa =>
    val runImport = GtfsFile.fromClasspath("test", getClass.getResource("simple-gtfs.zip")).use(Importer(_).run(xa))
    val runExport = GtfsFile("test", Path("exported.zip"), create = true).use { Exporter(_).run(xa) }
    for
      _ <- runImport
      _ <- runExport
    yield expect(true)
  }
}
