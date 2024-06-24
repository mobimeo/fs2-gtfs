package com.mobimeo.gtfs
package app

import cats.*
import cats.effect.*
import com.mobimeo.gtfs.file.GtfsFile
import fs2.*
import fs2.io.file.*
import model.*

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val transformed = for {
      in   <- GtfsFile[IO](Path(args(0)))
      out  <- GtfsFile[IO](Path(args(1)), create = true)
    } yield transform(in, out)
    transformed.use(_.as(ExitCode.Success))
  }

  private def transform(in: GtfsFile[IO], out: GtfsFile[IO]) =
    for {
      _ <- in.read.agencies.through(out.write.agencies).compile.drain
      _ <- in.read.attributions.through(out.write.attributions).compile.drain
      _ <- in.read.calendar.through(out.write.calendar).compile.drain
      _ <- in.read.calendarDates.through(out.write.calendarDates).compile.drain
      _ <- in.read.feedInfo.through(out.write.feedInfo).compile.drain
      _ <- in.read.routes.through(transformer(transformRoute)).through(out.write.routes).compile.drain
      _ <- in.read.stops.through(out.write.stops).compile.drain
      _ <- in.read.stopTimes.through(out.write.stopTimes).compile.drain
      _ <- in.read.trips.through(out.write.trips).compile.drain
    } yield ()

  private def transformer[T](transform: PartialFunction[T, T]): Pipe[IO, T, T] = _.map { t =>
    if (transform.isDefinedAt(t)) transform(t)
    else t
  }

  private object SbahnExtractor {
    def unapply(shortName: Option[String]): Option[String] = shortName.flatMap("^(S\\d+)$".r.findFirstIn)
  }

  private val transformRoute: PartialFunction[Route, Route] = {
    case r @ Route(_, _, SbahnExtractor(_), _, _, RouteType.Rail, _, _, _, _) => r.copy(tpe = RouteType.SuburbanRailway)
  }
}
