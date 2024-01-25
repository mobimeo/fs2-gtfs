package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import com.mobimeo.gtfs.model.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*
import fs2.*

class Exporter[F[_]: Sync](gtfsFile: GtfsFile[F]):
  private val tenant = gtfsFile.tenant

  def run(xa: Transactor[F]): F[Unit] =
    val a = sql"select * from agency where tenant = $tenant"
      .query[Table.agency.Columns]
      .streamWithChunkSize(1000)
      .transact(xa)
      .map(tuple => Agency(tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8, tuple._9, tuple._10))
      .through(gtfsFile.write.agencies)
      .compile
      .drain
    val b = sql"select * from calendar_date where tenant = $tenant"
      .query[Table.calendarDate.Columns]
      .streamWithChunkSize(1000)
      .transact(xa)
      .map(tuple => CalendarDate(tuple._2, tuple._3, tuple._4))
      .through(gtfsFile.write.agencies)
      .compile
      .drain
    a *> b
