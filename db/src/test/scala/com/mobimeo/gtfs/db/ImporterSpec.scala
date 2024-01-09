package com.mobimeo.gtfs.db

import cats.implicits.*
import doobie.*
import doobie.implicits.*
import weaver.*

object ImporterSpec extends InMemoryH2Suite {
  test("imports from gtfs-simple.zip") { case (xa, gtfsFile) =>
    for
      imported       <- Importer(gtfsFile)(xa)
      agencies       <- sql"select count(*) from agency".query[Int].unique.transact(xa)
      calendarDates  <- sql"select count(*) from calendar_date".query[Int].unique.transact(xa)
      feedInfo       <- sql"select count(*) from feed_info".query[Int].unique.transact(xa)
      routes         <- sql"select count(*) from route".query[Int].unique.transact(xa)
      stopTimes      <- sql"select count(*) from stop_time".query[Int].unique.transact(xa)
    yield List(
      expect(agencies == 3),
      expect(calendarDates == 5),
      expect(feedInfo == 1),
      expect(routes == 6),
      expect(stopTimes == 12),
    ).combineAll
  }
}
