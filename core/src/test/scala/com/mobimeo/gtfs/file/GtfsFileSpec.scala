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
    GtfsFile("simple", Path.fromNioPath(JPath.of(getClass.getResource("simple-gtfs.zip").toURI)))

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

  test("stops can be read") { gtfsFile =>
    for
      stops <- gtfsFile.read.stops[Stop].compile.toList
    yield List(
      expect(stops.size == 19),
      expect(stops(0).id == "8002041_4"),
      expect(stops(0).name == "Frankfurt(Main)Süd - Gleis 4".some),
      expect(stops(0).timezone == ZoneId.of("Europe/Berlin").some),
      expect(stops(0).lat == 50.099365.some),
      expect(stops(0).lon == 8.686457.some),
      expect(stops(0).locationType == LocationType.fromOrdinal(0).some),
      expect(stops(0).locationType == LocationType.fromOrdinal(0).some),
      expect(stops(0).parentStation == "1856_STATION".some),
      expect(stops(0).platformCode == "4".some),
      expect(stops(0).platformCode == "4".some),
      expect(stops(0).wheelchairBoarding == 1.some),
      expect(stops(0).code == "Frankfurt(Main)Süd - Gleis 4".some),
    ).combineAll
  }

  test("transfers can be read") { gtfsFile =>
    for
      transfers <- gtfsFile.read.transfers[Transfer].compile.toList
    yield List(
      expect(transfers.size == 13),
      expect(transfers(0).fromStopId == "8000084_1"),
      expect(transfers(0).toStopId == "8000084_4"),
      expect(transfers(0).transferType == TransferType.MinimumTimeRequiredTransfer),
      expect(transfers(0).minTransferTime == 300.some)
    ).combineAll
  }

  test("trips can be read") { gtfsFile =>
    for
      trips <- gtfsFile.read.trips[Trip].compile.toList
    yield List(
      expect(trips.size == 5),
      expect(trips(0).id == "DE:80:47:22"),
      expect(trips(0).routeId == "DE:80:EC47"),
      expect(trips(0).serviceId == "DE:80:47:22"),
      expect(trips(0).headsign == Option.empty),
      expect(trips(0).shortName == Option.empty),
      expect(trips(0).directionId == Option.empty),
      expect(trips(0).blockId == Option.empty),
      expect(trips(0).shapeId == Option.empty),
      expect(trips(0).wheelchairAccessible == 0.some),
      expect(trips(0).bikesAllowed == 0.some)
    ).combineAll
  }
}
