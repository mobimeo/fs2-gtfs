package com.mobimeo.gtfs
package db

import doobie._
import doobie.implicits._
import cats.effect._
import cats.syntax.all._
import fs2.Pipe
import fs2.Chunk
import model._

object GtfsDb {

  def createTable[A](implicit table: HasTable[A]) =
    Fragment.const(s"""
                 |create table ${table.tableName} (
                 | ${table.columns.mkString(",\n")}
                 |);""".stripMargin)

  def createTables[F[_]: Async](xa: Transactor[F]): F[GtfsDb[F]] =
    (for {
      _ <- createTable[Agency].update.run
      _ <- createTable[Stop].update.run
      _ <- createTable[Transfer].update.run
    } yield ()).transact(xa) as new GtfsDb(xa)

  def normalizeTableName(name: String): String =
    name.replace('.', '_')

  def quoteFieldName(name: String): String =
    s""""$name""""
}

final class GtfsDb[F[_]: Async](xa: Transactor[F]) extends Gtfs[F, Read, Write] {
  object read   extends GtfsDbRead(xa)
  object write  extends GtfsDbWrite(xa)
  object has    extends GtfsDbHas(xa)
  object delete extends GtfsDbDelete(xa)
}

abstract class GtfsDbHas[F[_]: Async](xa: Transactor[F]) extends GtfsHas[F] {

  def file(table: String): F[Boolean] =
    sql"select 1 from ${Fragment.const(GtfsDb.normalizeTableName(table))}"
      .query[Int]
      .unique
      .transact(xa)
      .attemptSql
      .map(_.isRight)

}

abstract class GtfsDbDelete[F[_]: Async](xa: Transactor[F]) extends GtfsDelete[F] {

  def file(table: String): F[Unit] =
    sql"drop table ${Fragment.const(GtfsDb.normalizeTableName(table))}".update.run.transact(xa).void

}

abstract class GtfsDbRead[F[_]: Async](xa: Transactor[F]) extends GtfsRead[F, Read] {

  private def select[R: Read](table: String) =
    sql"select * from ${Fragment.const(table)}".query[R]

  def file[R: Read](name: String): fs2.Stream[F, R] =
    select(GtfsDb.normalizeTableName(name)).stream.transact(xa)

  def joinOnEqual[A: Read, B: Read](ca: Column.Type[A], cb: Column.Type[B])(implicit
      evA: HasTable[A],
      evB: HasTable[B]
  ) = {
    val tableAName = Fragment.const(evA.tableName)
    val tableBName = Fragment.const(evB.tableName)
    val q          = sql"""
     | select * 
     |   from $tableAName as a
     |   join $tableBName as b
     |     on a.${Fragment.const(ca)} = b.${Fragment.const(cb)}           
     """.stripMargin

    println(q)

    q.query[(A, B)].stream
  }

}

abstract class GtfsDbWrite[F[_]: Async](xa: Transactor[F]) extends GtfsWrite[F, Write] {

  private def insertMany[W: Write](table: String, ws: Chunk[W]) = {
    val values = List.fill(Write[W].length)("?").mkString("(", ",", ")")
    val sql    = s"insert into $table values $values"
    Update[W](sql).updateMany(ws)
  }

  def file[W: Write](name: String): Pipe[F, W, Nothing] =
    _.chunkMin(512).evalTap { ws =>
      insertMany(GtfsDb.normalizeTableName(name), ws).transact(xa)
    }.drain

}
