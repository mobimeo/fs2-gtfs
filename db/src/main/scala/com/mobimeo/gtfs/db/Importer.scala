package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*
import fs2.*
import java.net.URL
import java.time.*

class Importer[F[_]: Sync](f: GtfsFile[F]):
  private val tenant = f.tenant

  def run(xa: Transactor[F]): F[Unit] =
    import Table.*
    for
      _  <- sql"insert into tenant (id) values (${f.tenant})".update.run.transact(xa)
      _  <- importEntity(agency,       f.read.agencies[model.Agency],                       xa)
      _  <- importEntity(calendarDate, f.read.calendarDates[model.CalendarDate],            xa)
      _  <- importEntity(feedInfo,     f.read.feedInfo[model.FeedInfo],                     xa)
      _  <- importEntity(stop,         f.read.stops[model.Stop],                            xa)
      _  <- importEntity(route,        f.read.routes[model.Route[model.ExtendedRouteType]], xa)
      _  <- importEntity(stopTime,     f.read.stopTimes[model.StopTime],                    xa)
      _  <- importEntity(transfer,     f.read.transfers[model.Transfer],                    xa)
      _  <- importEntity(trip,         f.read.trips[model.Trip],                            xa)
    yield ()

  private def importEntity[F[_], E, C](table: Table, entities: Stream[F, E], xa: Transactor[F])(using E => C, Sync[F], Write[ C ]) =
    entities
      .chunkN(10000)
      .map(_.toList)
      .map(toUpdate(table.insertInto, implicitly))
      .evalMap(_.transact(xa))
      .debug(count => s"Stored ${count} entries", println)
      .compile
      .fold(0)(_ + _)

  private def toUpdate[E, C: Write](sql: Fragment, toColumns: E => C)(l: List[E]) =
    Update[C](sql.update.sql).updateMany(l.map(toColumns))

  given Function[model.Agency, Table.agency.Columns]                        = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.CalendarDate, Table.calendarDate.Columns]            = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.FeedInfo, Table.feedInfo.Columns]                    = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.Route[model.ExtendedRouteType], Table.route.Columns] = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.Stop, Table.stop.Columns]                            = entity => tenant *: (
      entity.id,
      entity.code,
      entity.name,
      entity.desc,
      entity.lon.flatMap(lon => entity.lat.map(lat => lon -> lat)).map(model.Coordinate.apply.tupled),
      entity.zoneId,
      entity.url,
      entity.locationType,
      entity.parentStation,
      entity.timezone,
      entity.wheelchairBoarding,
      entity.levelId,
      entity.platformCode
    )
  given Function[model.StopTime, Table.stopTime.Columns]                    = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.Transfer, Table.transfer.Columns]                    = entity => tenant *: Tuple.fromProductTyped(entity)
  given Function[model.Trip, Table.trip.Columns]                            = entity => tenant *: Tuple.fromProductTyped(entity)
