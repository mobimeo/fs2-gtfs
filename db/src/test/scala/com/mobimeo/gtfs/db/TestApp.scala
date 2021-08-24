package com.mobimeo.gtfs.db

import cats.effect.IOApp
import cats.effect.IO

import cats.effect.ExitCode
import java.nio.file.Paths
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model._
import doobie._
import doobie.implicits._
import fs2.data.csv.CsvRowDecoder
import fs2.Stream
import com.mobimeo.gtfs.StandardName
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor

object TestApp extends IOApp {

  val transactor: Resource[IO, HikariTransactor[IO]] = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
      url = "jdbc:sqlite:gtfs.db",
      driverClassName = "org.sqlite.JDBC",
      user = "",
      pass = "",
      connectEC = ce
    )
  } yield xa

  def run(args: List[String]): IO[ExitCode] =
    transactor.use { xa =>
      GtfsDb.createTables(xa) *> IO.println("here") *> GtfsFile[IO](
        Paths.get(this.getClass().getClassLoader().getResource("google_transit.zip").toURI())
      ).use { gtfs =>
        val gtfsDb = new GtfsDb(xa)

        def copy[W: Write: CsvRowDecoder[*, String]](fileName: StandardName) =
          gtfs.read.file[W](fileName) through gtfsDb.write.file[W](fileName)

        val copyAll = Stream(
          copy[Stop](StandardName.Stops),
          copy[Transfer](StandardName.Transfers),
          copy[Agency](StandardName.Agency)
        ).parJoinUnbounded

        val join = gtfsDb.read
          .joinOnEqual(tables.Stop.id, tables.Transfer.fromStopId)
          .transact(xa)
        (copyAll ++ join).evalMap(x => IO.println(x.toString)).compile.drain as ExitCode.Success

      }
    }

}
