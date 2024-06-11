package com.mobimeo.gtfs.db

import cats.implicits.*
import com.mobimeo.gtfs.file.GtfsFile
import doobie.*
import doobie.implicits.*
import weaver.*

object ImporterSpec extends PostgresSuite {
  test("imports from gtfs to db") { xa =>
    val provider = "test"
    GtfsFile.fromClasspath(provider, getClass.getResource("simple-gtfs.zip")).use { gtfsFile =>
      for
        imported       <- Importer(gtfsFile).run(xa)
        agencies       <- sql"SELECT count(*) FROM agencies WHERE provider = $provider".query[Int].unique.transact(xa)
        calendars      <- sql"SELECT count(*) FROM calendars WHERE provider = $provider".query[Int].unique.transact(xa)
        calendarDates  <- sql"SELECT count(*) FROM calendar_dates WHERE provider = $provider".query[Int].unique.transact(xa)
        feedInfo       <- sql"SELECT count(*) FROM feed_infos WHERE provider = $provider".query[Int].unique.transact(xa)
        routes         <- sql"SELECT count(*) FROM routes WHERE provider = $provider".query[Int].unique.transact(xa)
        stopTimes      <- sql"SELECT count(*) FROM stop_times WHERE provider = $provider".query[Int].unique.transact(xa)
        stops          <- sql"SELECT count(*) FROM stops WHERE provider = $provider".query[Int].unique.transact(xa)
        transfers      <- sql"SELECT count(*) FROM transfers WHERE provider = $provider".query[Int].unique.transact(xa)
        trips          <- sql"SELECT count(*) FROM trips WHERE provider = $provider".query[Int].unique.transact(xa)
      yield Seq(
        expect(agencies == 3),
        expect(calendars == 5),
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
