package com.mobimeo.gtfs.db

import cats.effect.IOApp
import cats.effect.IO

import doobie.implicits._
import cats.effect.ExitCode
import com.mobimeo.gtfs.Gtfs
import java.nio.file.Paths
import com.mobimeo.gtfs.model.Agency

object TestApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    val xa = GtfsDb.xa[IO]

    // val y = xa.yolo
    // import y._
    // Tables.Agency.insert(Agency("id", "name", "url", ZoneId.of("UTC"), None, None, None, None)).update.check *>

    Gtfs[IO](Paths.get(this.getClass().getClassLoader().getResource("google_transit.zip").toURI())).use { gtfs =>
      GtfsDb.create(xa, gtfs) *> {
        val select = sql"select * from agency where agency_id='1'".query[Agency].unique
        select.transact(xa).flatMap(agency => IO.println(agency.toString)) as ExitCode.Success
      }
    }
  }

}
