package com.mobimeo.gtfs.db

import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import doobie.*
import doobie.implicits.*
import weaver.*

object ImporterSpec extends PostgresSuite {
  test("imports from gtfs to db") { xa =>
    val tenant = "test"
    GtfsFile.fromClasspath(tenant, getClass.getResource("simple-gtfs.zip")).use { gtfsFile =>
      for
        imported       <- Importer(gtfsFile).run(xa)
        agencies       <- sql"select count(*) from agency where tenant = $tenant".query[Int].unique.transact(xa)
        calendarDates  <- sql"select count(*) from calendar_date where tenant = $tenant".query[Int].unique.transact(xa)
        feedInfo       <- sql"select count(*) from feed_info where tenant = $tenant".query[Int].unique.transact(xa)
        routes         <- sql"select count(*) from route where tenant = $tenant".query[Int].unique.transact(xa)
        stopTimes      <- sql"select count(*) from stop_time where tenant = $tenant".query[Int].unique.transact(xa)
        stops          <- sql"select count(*) from stop where tenant = $tenant".query[Int].unique.transact(xa)
        transfers      <- sql"select count(*) from transfer where tenant = $tenant".query[Int].unique.transact(xa)
        trips          <- sql"select count(*) from trip where tenant = $tenant".query[Int].unique.transact(xa)
      yield Seq(
        expect(agencies == 3),
        expect(calendarDates == 5),
        expect(feedInfo == 1),
        expect(routes == 6),
        expect(stopTimes == 12),
        expect(stops == 18),
        expect(transfers == 13),
        expect(trips == 5)
      ).combineAll
    }
  }
}
