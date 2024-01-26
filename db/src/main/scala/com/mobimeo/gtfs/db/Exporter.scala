package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model.*
import doobie.*
import doobie.implicits.*
import fs2.*

class Exporter[F[_]: Sync](gtfsFile: GtfsFile[F]):
  private val tenant = gtfsFile.tenant

  def run(xa: Transactor[F]): F[Unit] =
    val files = List(
      exportEntity(Table.agency,       gtfsFile.write.agencies,      xa),
      exportEntity(Table.calendarDate, gtfsFile.write.calendarDates, xa),
      exportEntity(Table.feedInfo,     gtfsFile.write.feedInfo,      xa),
      exportEntity(Table.route,        gtfsFile.write.routes,        xa),
      exportEntity(Table.stopTime,     gtfsFile.write.stopTimes,     xa),
      exportEntity(Table.stop,         gtfsFile.write.stops,         xa),
      exportEntity(Table.transfer,     gtfsFile.write.transfers,     xa),
      exportEntity(Table.trip,         gtfsFile.write.trips,         xa))
    files
      .map(_.compile.drain)
      .sequence
      .map(_.combineAll)

  private def exportEntity[F[_], E](table: Table[E], writer: Pipe[F, E, Nothing], xa: Transactor[F])(using Sync[F]): Stream[F, E] =
    selectEntity(table, xa).through(writer)

  private def selectEntity[F[_], E](table: Table[E], xa: Transactor[F])(using Sync[F]): Stream[F, E] =
    table.selectAll(tenant)
      .streamWithChunkSize(1000)
      .transact(xa)
      .map(table.toModel)
