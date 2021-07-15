package com.mobimeo.gtfs
package db

import doobie._
import doobie.implicits._
import cats.effect._
import cats.syntax.all._
import model._
import fs2.Pipe
import Tables.TableSchema
import fs2.Chunk

object GtfsDb {

  def createTables[F[_]: Async](xa: Transactor[F]): F[GtfsDb[F]] =
    (for {
      _ <- TableSchema[Route[Int]].create(StandardName.Routes).update.run
      _ <- TableSchema[Stop].create(StandardName.Stops).update.run
      _ <- TableSchema[Transfer].create(StandardName.Transfers).update.run
      _ <- TableSchema[StopTime].create(StandardName.StopTimes).update.run
      _ <- TableSchema[Agency].create(StandardName.Agency).update.run
      _ <- TableSchema[Trip].create(StandardName.Trips).update.run
      _ <- TableSchema[Calendar].create(StandardName.Calendar).update.run
      _ <- TableSchema[CalendarDate].create(StandardName.CalendarDates).update.run
      _ <- TableSchema[FareAttribute].create(StandardName.FareAttributes).update.run
      _ <- TableSchema[FareRules].create(StandardName.FareRules).update.run
      _ <- TableSchema[Shape].create(StandardName.Shapes).update.run
      _ <- TableSchema[Frequency].create(StandardName.Frequencies).update.run
      _ <- TableSchema[Pathway].create(StandardName.Pathways).update.run
      _ <- TableSchema[Level].create(StandardName.Levels).update.run
      _ <- TableSchema[FeedInfo].create(StandardName.FeedInfo).update.run
      _ <- TableSchema[Translation].create(StandardName.Translations).update.run
      _ <- TableSchema[Attribution].create(StandardName.Attributions).update.run
    } yield ()).transact(xa) as new GtfsDb(xa)

  def normalizeTableName(name: String): String =
    name.replace('.', '_')

  def quoteFieldName(name: String): String =
    s""""$name""""
}

final class GtfsDb[F[_]: Async](xa: Transactor[F]) extends Gtfs[F, Read, Write] {
  def hasFile(name: String): F[Boolean] =
    sql"select 1 from ${Fragment.const(name)}".query[Int].unique.transact(xa).attempt.map(_.fold(_ => false, _ => true))

  object read extends GtfsDbRead[F](xa)

  object write extends GtfsDbWrite[F](xa)
}

abstract class GtfsDbRead[F[_]: Async](xa: Transactor[F]) extends GtfsRead[F, Read] {

  private def select[R: Read](table: String) =
    sql"select * from ${Fragment.const(table)}".query[R]

  def file[R: Read](name: String): fs2.Stream[F, R] =
    select(GtfsDb.normalizeTableName(name)).stream.transact(xa)

}

abstract class GtfsDbWrite[F[_]: Async](xa: Transactor[F]) extends GtfsWrite[F, Write] {

  def insertMany[W: Write](table: String, ws: Chunk[W]) = {
    val values = List.fill(Write[W].length)("?").mkString("(", ",", ")")
    val sql    = s"insert into $table values $values"
    Update[W](sql).updateMany(ws)
  }

  def file[W: Write](name: String): Pipe[F, W, Nothing] =
    _.chunkMin(512).evalTap { ws =>
      insertMany(GtfsDb.normalizeTableName(name), ws).transact(xa)
    }.drain

}
