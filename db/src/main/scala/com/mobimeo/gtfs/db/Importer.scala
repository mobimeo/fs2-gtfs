package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model
import com.mobimeo.gtfs.model.ExceptionType
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*
import fs2.*
import java.time.*

object Importer {

  def apply[F[_]: Sync](f: GtfsFile[F])(xa: Transactor[F]): F[Unit] =
    for
      _ <- importEntity(f.read.agencies[model.Agency], Table.Agency)(xa)
      _ <- importEntity(f.read.calendarDates[model.CalendarDate], Table.CalendarDate)(xa)
      _ <- importEntity(f.read.feedInfo[model.FeedInfo], Table.FeedInfo)(xa)
      _ <- importEntity(f.read.routes[model.Route[model.ExtendedRouteType]], Table.Route)(xa)
      _ <- importEntity(f.read.stopTimes[model.StopTime], Table.StopTime)(xa)
      _ <- importEntity(f.read.stops[model.Stop], Table.Stop)(xa)
      _ <- importEntity(f.read.transfers[model.Transfer], Table.Transfer)(xa)
      _ <- importEntity(f.read.trips[model.Trip], Table.Trip)(xa)
    yield ()

  private def importEntity[F[_]: Sync, T: Write](x: Stream[F, T], table: Table)(xa: Transactor[F]) =
    x.compile.toList.flatMap { xs => Update[T](table.insertInto).updateMany(xs).transact(xa) }

  given Get[ExceptionType] = Get[String].map(ExceptionType.valueOf)
  given Put[ExceptionType] = Put[String].contramap(_.toString)

  given Get[ZoneId] = Get[String].map(ZoneId.of)
  given Put[ZoneId] = Put[String].contramap(_.getId)

  given Get[model.ExtendedRouteType] = Get[String].map(model.ExtendedRouteType.valueOf)
  given Put[model.ExtendedRouteType] = Put[String].contramap(_.toString)

  given Get[model.LocationType] = Get[String].map(model.LocationType.valueOf)
  given Put[model.LocationType] = Put[String].contramap(_.toString)

  given Get[model.PickupOrDropOffType] = Get[String].map(model.PickupOrDropOffType.valueOf)
  given Put[model.PickupOrDropOffType] = Put[String].contramap(_.toString)

  given Get[model.Timepoint] = Get[String].map(model.Timepoint.valueOf)
  given Put[model.Timepoint] = Put[String].contramap(_.toString)

  given Get[model.TransferType] = Get[String].map(model.TransferType.valueOf)
  given Put[model.TransferType] = Put[String].contramap(_.toString)

}
