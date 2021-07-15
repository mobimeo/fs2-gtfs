package com.mobimeo.gtfs.db

import cats.effect.IOApp
import cats.effect.IO

import cats.effect.ExitCode
import java.nio.file.Paths
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model._
import doobie._
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
      GtfsFile[IO](Paths.get(this.getClass().getClassLoader().getResource("google_transit.zip").toURI())).use { gtfs =>
        GtfsDb.createTables(xa).flatMap { gtfsDb =>
          def copy[W: Write: CsvRowDecoder[*, String]](fileName: StandardName) =
            gtfs.read.file[W](fileName) through gtfsDb.write.file[W](fileName)

          val copyAll = Stream(
            copy[Route[Int]](StandardName.Routes),
            copy[Stop](StandardName.Stops),
            copy[Transfer](StandardName.Transfers),
            copy[StopTime](StandardName.StopTimes),
            copy[Agency](StandardName.Agency),
            copy[Trip](StandardName.Trips),
            copy[Calendar](StandardName.Calendar),
            copy[CalendarDate](StandardName.CalendarDates),
            copy[FareAttribute](StandardName.FareAttributes),
            copy[FareRules](StandardName.FareRules),
            copy[Shape](StandardName.Shapes),
            copy[Frequency](StandardName.Frequencies),
            copy[Pathway](StandardName.Pathways),
            copy[Level](StandardName.Levels),
            copy[FeedInfo](StandardName.FeedInfo),
            copy[Translation](StandardName.Translations),
            copy[Attribution](StandardName.Attributions)
          ).parJoinUnbounded

          copyAll.compile.drain as ExitCode.Success
        }
      }
    }

}
