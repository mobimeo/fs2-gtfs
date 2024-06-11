package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import com.mobimeo.gtfs.*
import com.mobimeo.gtfs.file.GtfsFile
import fs2.*
import fs2.io.file.*
import doobie.util.transactor.Transactor

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    args.head match {
      case "-i" | "import" => doImport(args.tail.head).use_.as(ExitCode.Success)
      case "-e" | "export" => doExport(args.tail.head).use_.as(ExitCode.Success)
      case other           => IO.raiseError(new IllegalArgumentException(s" args $args not supported"))
    }

  private def doExport(arg: String) = for {
    gtfsFile   <- GtfsFile[IO]("db-fernverkehr", Path(arg), create = true)
    xa          = Transactor.fromDriverManager[IO](
                    "org.postgresql.Driver",
                    "jdbc:postgresql:gtfs",
                    "gunnar.bastkowski",
                    "",
                    logHandler = None)
    exported  <- Resource.eval(Exporter(gtfsFile).run(xa))
  } yield gtfsFile

  private def doImport(arg: String) = for {
    gtfsFile   <- GtfsFile[IO]("gtfs.de", Path(arg))
    xa          = Transactor.fromDriverManager[IO](
                    "org.postgresql.Driver",
                    "jdbc:postgresql:gtfs",
                    "gunnar.bastkowski",
                    "",
                    logHandler = None)
    _         <- Resource.eval(Schema.create(xa))
    imported  <- Resource.eval(Importer(gtfsFile).run(xa))
  } yield gtfsFile
}
