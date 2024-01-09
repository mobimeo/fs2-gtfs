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
      .map(_.create)
      .map(_.update.run.transact(xa))
      .sequence
      .map(_.sum)

  def drop[F[_]: Sync](xa: Transactor[F]): F[Unit] =
    tables
      .map(_.drop)
      .map(_.update.run.transact(xa))
      .sequence
      .as(())

  private val tables = List(
    Agency,
    CalendarDate,
    FeedInfo,
    Route,
    StopTime,
    Stop,
    TicketingIdentifier,
    Transfer,
    Trip
  )
}
