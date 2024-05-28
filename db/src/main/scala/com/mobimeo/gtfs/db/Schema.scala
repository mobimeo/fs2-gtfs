package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*

object Schema {
  import Table.*

  def create[F[_]: Sync](xa: Transactor[F]): F[Int] =
    tables
      .map(_._1)
      .map(_.update.run.transact(xa))
      .sequence
      .map(_.sum)

  def drop[F[_]: Sync](xa: Transactor[F]): F[Unit] =
    tables
      .map(_._2)
      .map(_.update.run.transact(xa))
      .sequence
      .as(())

  private val tables = List(
    (provider.create, provider.drop),
    (agency.create, agency.drop),
    (calendarDate.create, calendarDate.drop),
    (feedInfo.create, feedInfo.drop),
    (route.create, route.drop),
    (stopTime.create, stopTime.drop),
    (stop.create, stop.drop),
    (transfer.create, transfer.drop),
    (trip.create, trip.drop)
  )
}
