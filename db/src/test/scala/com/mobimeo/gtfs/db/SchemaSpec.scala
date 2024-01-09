package com.mobimeo.gtfs.db

import cats.effect.*
import doobie.*
import doobie.h2.*
import doobie.implicits.*
import weaver.*

object SchemaSpec extends IOSuite {
  override type Res = H2Transactor[IO]
  override def sharedResource: Resource[IO, Res] =
    for
      ce <- ExecutionContexts.fixedThreadPool[IO](4)
      xa <- H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "", ce)
    yield xa

  test("A database schema can be created") { xa =>
    for
      schema <- Schema.create[IO](xa)
      sql     = sql"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
      tables <- sql.query[String].to[Set].transact(xa)
    yield expect(tables == Set(
                   "TICKETING_IDENTIFIER",
                   "AGENCY",
                   "ROUTE",
                   "CALENDAR_DATE",
                   "TRIP",
                   "FEED_INFO",
                   "STOP",
                   "TRANSFER",
                   "STOP_TIME"
                 ))
  }
}
