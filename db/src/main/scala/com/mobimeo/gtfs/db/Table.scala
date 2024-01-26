package com.mobimeo.gtfs.db

import com.mobimeo.gtfs.model
import com.mobimeo.gtfs.model.ExceptionType
import com.mobimeo.gtfs.model.ExtendedRouteType
import com.mobimeo.gtfs.model.LocationType
import com.mobimeo.gtfs.model.PickupOrDropOffType
import com.mobimeo.gtfs.model.Timepoint
import com.mobimeo.gtfs.model.TransferType
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*
import java.net.URL
import java.time.*

object Table:
  object Tenant {
    val create = sql"CREATE TABLE IF NOT EXISTS tenant (id VARCHAR NOT NULL PRIMARY KEY)"
    val drop   = sql"DROP TABLE IF EXISTS tenant CASCADE"
    val insert = "INSERT INTO tenant (id) VALUES (?)"
 }

  object agency extends Table[model.Agency] {
    type Columns = (
      String,
      String,
      String,
      Option[URL],
      ZoneId,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      Option[String]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS agency (
        tenant                 VARCHAR NOT NULL REFERENCES tenant(id),
        id                     VARCHAR NOT NULL,
        name                   VARCHAR NOT NULL,
        url                    VARCHAR,
        timezone               VARCHAR NOT NULL,
        language               VARCHAR,
        fare_url               VARCHAR,
        phone                  VARCHAR,
        email                  VARCHAR,
        ticketing_deep_link_id VARCHAR,

        PRIMARY KEY (tenant, id)
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS agency CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO agency (tenant, id, name, url, timezone, language, phone, fare_url, email, ticketing_deep_link_id)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from agency where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.Agency): Columns = tenant *: Tuple.fromProductTyped(entity)
    def toModel(tuple: Columns): model.Agency = model.Agency.apply.tupled(tuple drop 1)
  }

  object calendarDate extends Table[model.CalendarDate] {
    type Columns = (
      String,
      String,
      LocalDate,
      ExceptionType
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS calendar_date (
        tenant VARCHAR NOT NULL REFERENCES tenant(id),
        service_id VARCHAR NOT NULL,
        date DATE NOT NULL,
        exception_type VARCHAR NOT NULL
      )"""
    val drop: Fragment = sql"DROP TABLE IF EXISTS calendar_date CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO calendar_date (tenant, service_id, date, exception_type)
      VALUES (?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from calendar_date where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.CalendarDate): Columns = tenant *: Tuple.fromProductTyped(entity)
    def toModel(tuple: Columns): model.CalendarDate = model.CalendarDate.apply.tupled(tuple drop 1)
  }

  object feedInfo extends Table[model.FeedInfo] {
    type Columns = (
      String,
      Option[String],
      String,
      String,
      String,
      Option[String],
      Option[LocalDate],
      Option[LocalDate],
      Option[String],
      Option[String]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS feed_info (
        tenant                VARCHAR NOT NULL REFERENCES tenant(id),
        feed_version          VARCHAR,
        feed_publisher_name   VARCHAR NOT NULL,
        feed_publisher_url    VARCHAR NOT NULL,
        feed_lang             VARCHAR NOT NULL,
        default_lang          VARCHAR,
        feed_start_date       DATE,
        feed_end_date         DATE,
        feed_contact_email    VARCHAR,
        feed_contact_url      VARCHAR
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS feed_info CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO feed_info (
        tenant,
        feed_version,
        feed_publisher_name,
        feed_publisher_url,
        feed_lang,
        default_lang,
        feed_start_date,
        feed_end_date,
        feed_contact_email,
        feed_contact_url
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from feed_info where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.FeedInfo): Columns = tenant *: Tuple.fromProductTyped(entity)
    def toModel(tuple: Columns): model.FeedInfo = model.FeedInfo.apply.tupled(tuple drop 1)
  }

  object route extends Table[model.Route[ExtendedRouteType]] {
    type Columns = (
      String,
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[String],
      ExtendedRouteType,
      Option[String],
      Option[String],
      Option[String],
      Option[Int]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS route (
        tenant VARCHAR NOT NULL REFERENCES tenant(id),
        route_id VARCHAR NOT NULL,
        agency_id VARCHAR,
        route_short_name VARCHAR,
        route_long_name VARCHAR,
        route_desc VARCHAR,
        route_type VARCHAR NOT NULL,
        route_url VARCHAR,
        route_color VARCHAR,
        route_text_color VARCHAR,
        route_sort_order INT,

        PRIMARY KEY (tenant, route_id),
        FOREIGN KEY (tenant, agency_id) REFERENCES agency (tenant, id)
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS route CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO route (
        tenant,
        route_id,
        agency_id,
        route_short_name,
        route_long_name,
        route_desc,
        route_type,
        route_url,
        route_color,
        route_text_color,
        route_sort_order
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from route where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.Route[ExtendedRouteType]): Columns =
      tenant *: Tuple.fromProductTyped(entity)
    def toModel(tuple: Columns): model.Route[ExtendedRouteType] =
      model.Route[ExtendedRouteType].apply.tupled(tuple drop 1)
  }

  object stopTime extends Table[model.StopTime] {
    type Columns = (
      String,
      String,
      LocalTime,
      LocalTime,
      String,
      Int,
      Option[String],
      Option[PickupOrDropOffType],
      Option[PickupOrDropOffType],
      Option[Double],
      Option[Timepoint]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS stop_time (
        tenant                VARCHAR NOT NULL REFERENCES tenant(id),
        trip_id               VARCHAR NOT NULL,
        arrival_time          TIME NOT NULL,
        departure_time        TIME NOT NULL,
        stop_id               VARCHAR NOT NULL,
        stop_sequence         VARCHAR NOT NULL,
        stop_headsign         VARCHAR,
        pickup_type           VARCHAR,
        drop_off_type         VARCHAR,
        shape_dist_traveled   VARCHAR,
        timepoint             VARCHAR
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS stop_time CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO stop_time (
        tenant,
        trip_id,
        arrival_time,
        departure_time,
        stop_id,
        stop_sequence,
        stop_headsign,
        pickup_type,
        drop_off_type,
        shape_dist_traveled,
        timepoint
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from stop_time where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.StopTime): Columns = tenant *: Tuple.fromProductTyped(entity)
    def toModel(tuple: Columns): model.StopTime = model.StopTime.apply.tupled(tuple drop 1)
  }

  object stop extends Table[model.Stop] {
    type Columns = (
      String,
      String,
      Option[String],
      Option[String],
      Option[String],
      Option[model.Coordinate],
      Option[String],
      Option[String],
      Option[LocationType],
      Option[String],
      Option[ZoneId],
      Option[Int],
      Option[String],
      Option[String]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS stop (
        tenant VARCHAR NOT NULL REFERENCES tenant(id),
        id VARCHAR NOT NULL,
        code VARCHAR,
        name VARCHAR,
        description VARCHAR,
        location geography(POINT,4326),
        zone_id VARCHAR,
        stop_url VARCHAR,
        location_type VARCHAR,
        parent_station VARCHAR,
        stop_timezone VARCHAR,
        wheelchair_boarding VARCHAR,
        level_id VARCHAR,
        platform_code VARCHAR,

        PRIMARY KEY (tenant, id)
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS stop CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO stop (
        tenant,
        id,
        code,
        name,
        description,
        location,
        zone_id,
        stop_url,
        location_type,
        parent_station,
        stop_timezone,
        wheelchair_boarding,
        level_id,
        platform_code
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from stop where tenant = $tenant".query[Columns]

    def toColumns(tenant: String)(entity: model.Stop): Columns = (
      tenant,
      entity.id,
      entity.code,
      entity.name,
      entity.desc,
      entity.lon.flatMap(lon => entity.lat.map(lat => lon -> lat)).map(model.Coordinate.apply.tupled),
      entity.zoneId,
      entity.url,
      entity.locationType,
      entity.parentStation,
      entity.timezone,
      entity.wheelchairBoarding,
      entity.levelId,
      entity.platformCode
    )
    def toModel(tuple: Columns): model.Stop = model.Stop(
      tuple._2,
      tuple._3,
      tuple._4,
      tuple._5,
      tuple._6.map(_.lat),
      tuple._6.map(_.lon),
      tuple._7,
      tuple._8,
      tuple._9,
      tuple._10,
      tuple._11,
      tuple._12,
      tuple._13,
      tuple._14
    )
  }

  object transfer extends Table[model.Transfer] {
    type Columns = (String, String, String, TransferType, Option[Int])

    // TODO PRIMARY KEY (from_stop_id, to_stop_id, from_trip_id, to_trip_id, from_route_id, to_route_id),
    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS transfer (
        tenant VARCHAR NOT NULL REFERENCES tenant(id),
        from_stop_id VARCHAR NOT NULL,
        to_stop_id VARCHAR NOT NULL,
        transfer_type VARCHAR NOT NULL,
        min_transfer_time VARCHAR NOT NULL,

        FOREIGN KEY (tenant, from_stop_id) REFERENCES stop (tenant, id),
        FOREIGN KEY (tenant, to_stop_id) REFERENCES stop (tenant, id)
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS transfer CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO transfer (
        tenant,
        from_stop_id,
        to_stop_id,
        transfer_type,
        min_transfer_time
      ) values (?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from transfer where tenant = $tenant".query[Columns]
    def toModel(tuple: Columns): model.Transfer = model.Transfer.apply.tupled(tuple drop 1)
  }

  object trip extends Table[model.Trip] {
    type Columns = (
      String,
      String,
      String,
      String,
      Option[String],
      Option[String],
      Option[Int],
      Option[String],
      Option[String],
      Option[Int],
      Option[Int]
    )

    val create: Fragment = sql"""
      CREATE TABLE IF NOT EXISTS trip (
        tenant VARCHAR NOT NULL REFERENCES tenant(id),
        route_id VARCHAR NOT NULL,
        service_id VARCHAR NOT NULL,
        trip_id VARCHAR NOT NULL,
        trip_headsign VARCHAR,
        trip_short_name VARCHAR,
        direction_id VARCHAR,
        block_id VARCHAR,
        shape_id VARCHAR,
        wheelchair_accessible VARCHAR,
        bikes_allowed VARCHAR,

        PRIMARY KEY (tenant, trip_id),
        FOREIGN KEY (tenant, route_id) REFERENCES route (tenant, route_id)
      )"""

    val drop: Fragment = sql"DROP TABLE IF EXISTS trip CASCADE"

    val insertInto: Fragment = sql"""
      INSERT INTO trip (
        tenant,
        route_id,
        service_id,
        trip_id,
        trip_headsign,
        trip_short_name,
        direction_id,
        block_id,
        shape_id,
        wheelchair_accessible,
        bikes_allowed
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""

    def selectAll(tenant: String): Query0[Columns] = sql"select * from trip where tenant = $tenant".query[Columns]
    def toModel(tuple: Columns): model.Trip = model.Trip.apply.tupled(tuple drop 1)
  }

trait Table[T] {
  type Columns
  val create: Fragment
  val drop: Fragment
  val insertInto: Fragment
  def selectAll(tenant: String): Query0[Columns]
  def toModel(tuple: Columns): T
}
