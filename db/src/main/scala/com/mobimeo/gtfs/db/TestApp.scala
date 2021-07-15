package com.mobimeo.gtfs.db

import cats.effect.IOApp
import cats.effect.IO

import cats.effect.ExitCode
import java.nio.file.Paths
import com.mobimeo.gtfs.model.Agency
import com.mobimeo.gtfs.file.GtfsFile

object TestApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    val xa = GtfsDb.xa[IO]
       
    GtfsFile[IO](Paths.get(this.getClass().getClassLoader().getResource("google_transit.zip").toURI())).use { gtfs =>
      GtfsDb.create(xa).flatMap { gtfsDb =>
        val copy = gtfs.read.file[Agency]("agency.txt") through gtfsDb.write.file[Agency]("agency")
        val read = gtfsDb.read.file[Agency]("agency")
        
        (copy ++ read).evalTap(agency => IO.println(agency.toString)).compile.drain as ExitCode.Success
      }
    }
  }

}
