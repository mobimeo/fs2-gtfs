package com.mobimeo.gtfs.db

import cats.*
import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*

object Schema {
  import Table.*
  def create[F[_]: Sync](xa: Transactor[F]): F[Int] = {
    List(
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
      .map(_.createTable.update.run.transact(xa))
      .sequence
      .map(_.sum)
  }
}
