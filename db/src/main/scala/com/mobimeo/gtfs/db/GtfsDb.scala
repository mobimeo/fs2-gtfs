package com.mobimeo.gtfs.db

import doobie._, doobie.implicits._
import cats.effect._
import com.mobimeo.gtfs.Gtfs
import com.mobimeo.gtfs.model.Agency
import cats.syntax.all._

object GtfsDb {

  def xa[F[_]: Async] =
    Transactor.fromDriverManager[F](
      driver = "org.sqlite.JDBC",
      url = "jdbc:sqlite:gtfs.db"
    )

  val createTables = Tables.Agency.create

  def create[F[_]: Async](xa: Transactor[F], gtfs: Gtfs[F]) =
    createTables.update.run.transact(xa) *> {
      gtfs.read.agencies[Agency].evalMap(agency => Tables.Agency.insert(agency).update.run.transact(xa)).compile.drain
    }

}
