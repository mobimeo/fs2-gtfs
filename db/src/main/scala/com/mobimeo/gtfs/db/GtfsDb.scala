package com.mobimeo.gtfs.db

import doobie._, doobie.implicits._
import cats.effect._
import cats.syntax.all._
import com.mobimeo.gtfs.GtfsRead
import com.mobimeo.gtfs
import fs2.Pipe

object GtfsDb {

  def xa[F[_]: Async] =
    Transactor.fromDriverManager[F](
      driver = "org.sqlite.JDBC",
      url = "jdbc:sqlite:gtfs.db"
    )

  val createTables = Tables.Agency.create

  def create[F[_]: Async](xa: Transactor[F]): F[GtfsDb[F]] =
    createTables.update.run.transact(xa) as new GtfsDb(xa)        
}

final class GtfsDb[F[_]: Async](xa: Transactor[F]) extends gtfs.Gtfs[F, Read, Write] {
  def hasFile(name: String): F[Boolean] = ???

  object read extends GtfsDbRead[F](xa)

  object write extends GtfsDbWrite[F](xa)
}

abstract class GtfsDbRead[F[_]: Async](xa: Transactor[F]) extends GtfsRead[F, Read] {

  val y = xa.yolo
  import y._

  private def select[R: Read](table: String) =
    sql"select * from ${Fragment.const(table)}".query[R]

  def file[R: Read](name: String): fs2.Stream[F, R] = {
      val q = select(name)
      val s = fs2.Stream.eval[F, Unit](q.check).drain
      s ++ q.stream.transact(xa)
  }
}

abstract class GtfsDbWrite[F[_]: Async](xa: Transactor[F]) extends gtfs.GtfsWrite[F, Write] {

  val y = xa.yolo
  import y._

  private def insert[W: Write](table: String, w: W) =
    sql"insert into ${Fragment.const(table)} values (${Fragments.values(w)})".update


  def file[W: Write](name: String): Pipe[F, W, Nothing] =
    _.evalTap { w => 
        val q = insert(name, w)
        q.check *> q.run.transact(xa)
    }.drain

}
