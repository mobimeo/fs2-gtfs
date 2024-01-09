package com.mobimeo.gtfs.file

import cats.effect.*
import cats.implicits.*
import com.mobimeo.gtfs.model.*
import fs2.io.file.*
import java.nio.file.{Path => JPath}
import java.time.*
import weaver.*

object GtfsFileSpec extends IOSuite {
  override type Res = GtfsFile[IO]
  override def sharedResource: Resource[IO, Res] =
    GtfsFile(Path.fromNioPath(JPath.of(getClass.getResource("simple-gtfs.zip").toURI)))

  test("agencies can be read") { gtfsFile =>
    for
      agencies <- gtfsFile.read.agencies[Agency].compile.toList
    yield List(
      expect(agencies.size == 3),
      expect(agencies(0).name == "An Agency"),
      expect(agencies(1).id == "B"),
      expect(agencies(2).timezone == ZoneId.of("Europe/Berlin")),
    ).combineAll
  }

  test("calendarDates can be read") { gtfsFile =>
    for
      calendarDates <- gtfsFile.read.calendarDates[CalendarDate].compile.toList
    yield List(
      expect(calendarDates.size == 5),
      expect(calendarDates(0).serviceId == "DE:10:27:12"),
      expect(calendarDates(3).date == LocalDate.of(2023, 10, 23)),
      expect(calendarDates(4).serviceId == "DE:70:777:201")
    ).combineAll
  }

  test("feedInfo can be read") { gtfsFile =>
    for
      feedInfo <- gtfsFile.read.feedInfo[FeedInfo].compile.toList
    yield List(
      expect(feedInfo.size == 1),
      expect(feedInfo(0).version == "2".some)
    ).combineAll
  }

  test("routes can be read") { gtfsFile =>
    for
      routes <- gtfsFile.read.routes[Route[ExtendedRouteType]].compile.toList
    yield List(
      expect(routes.size == 6),
      expect(routes(0).id == "DE:BUS10")
    ).combineAll
  }

  test("stopTimes can be read") { gtfsFile =>
    for
      stopTimes <- gtfsFile.read.stopTimes[StopTime].compile.toList
    yield List(
      expect(stopTimes.size == 12),
      expect(stopTimes(0).tripId == "DE:1"),
      expect(stopTimes(1).arrivalTime == LocalTime.of(18, 39, 0)),
      expect(stopTimes(1).departureTime == LocalTime.of(18, 42, 0)),
      expect(stopTimes(1).stopSequence == 2),
      expect(stopTimes(1).stopId == "8070003"),
      expect(stopTimes(1).stopHeadsign == "Bruxelles-Midi".some),
    ).combineAll
  }
}
