package com.mobimeo.gtfs.db

import doobie.*
import doobie.implicits.*

object Table:
  object Agency extends Table(
    sql"""
      CREATE TABLE IF NOT EXISTS agency (
        id VARCHAR NOT NULL UNIQUE,
        name VARCHAR NOT NULL,
        url VARCHAR,
        timezone VARCHAR NOT NULL,
        language VARCHAR,
        fare_url VARCHAR,
        phone VARCHAR,
        email VARCHAR,
        ticketing_deep_link_id VARCHAR
      )
    """,
    sql"DROP table agency",
    """
      INSERT INTO agency (id, name, url, timezone, language, phone, fare_url, email, ticketing_deep_link_id)
      values (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """
  )

  object CalendarDate extends Table(
    sql"""CREATE TABLE IF NOT EXISTS calendar_date (
      service_id VARCHAR NOT NULL,
      date VARCHAR NOT NULL,
      exception_type VARCHAR NOT NULL
    )""",
    sql"DROP table calendar_date",
    "INSERT INTO calendar_date (service_id, date, exception_type) values (?, ?, ?)"
  )

  object FeedInfo extends Table(
    sql"""CREATE TABLE IF NOT EXISTS feed_info (
      feed_version VARCHAR,
      feed_publisher_name VARCHAR NOT NULL,
      feed_publisher_url VARCHAR NOT NULL,
      feed_lang VARCHAR NOT NULL,
      default_lang VARCHAR,
      feed_start_date VARCHAR,
      feed_end_date VARCHAR,
      feed_contact_email VARCHAR,
      feed_contact_url VARCHAR
    )""",
    sql"DROP table feed_info",
    """INSERT INTO feed_info (
      feed_version,
      feed_publisher_name,
      feed_publisher_url,
      feed_lang,
      default_lang,
      feed_start_date,
      feed_end_date,
      feed_contact_email,
      feed_contact_url
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

  object Route extends Table(
    sql"""CREATE TABLE IF NOT EXISTS route (
      route_id VARCHAR NOT NULL,
      agency_id VARCHAR,
      route_short_name VARCHAR,
      route_long_name VARCHAR,
      route_desc VARCHAR,
      route_type VARCHAR NOT NULL,
      route_url VARCHAR,
      route_color VARCHAR,
      route_text_color VARCHAR,
      route_sort_order INT
    )""",
    sql"DROP table route",
    """INSERT INTO route (
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
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

  object StopTime extends Table(
    sql"""CREATE TABLE IF NOT EXISTS stop_time (
      trip_id VARCHAR NOT NULL,
      arrival_time VARCHAR NOT NULL,
      departure_time VARCHAR NOT NULL,
      stop_id VARCHAR NOT NULL,
      stop_sequence VARCHAR NOT NULL,
      stop_headsign VARCHAR,
      pickup_type VARCHAR,
      drop_off_type VARCHAR,
      shape_dist_traveled VARCHAR,
      timepoint VARCHAR
    )""",
    sql"DROP table stop_time",
    """INSERT INTO stop_time (
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
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

  object Stop extends Table(
    sql"""CREATE TABLE IF NOT EXISTS stop (
      stop_id VARCHAR NOT NULL,
      stop_code VARCHAR,
      stop_name VARCHAR,
      stop_desc VARCHAR,
      stop_lat VARCHAR,
      stop_lon VARCHAR,
      zone_id VARCHAR,
      stop_url VARCHAR,
      location_type VARCHAR,
      parent_station VARCHAR,
      stop_timezone VARCHAR,
      wheelchair_boarding VARCHAR,
      level_id VARCHAR,
      platform_code VARCHAR
    )""",
    sql"DROP table stop",
    """INSERT INTO stop (
      stop_id,
      stop_code,
      stop_name,
      stop_desc,
      stop_lat,
      stop_lon,
      zone_id,
      stop_url,
      location_type,
      parent_station,
      stop_timezone,
      wheelchair_boarding,
      level_id,
      platform_code
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

  object TicketingIdentifier extends Table(
    sql"""CREATE TABLE IF NOT EXISTS ticketing_identifier (
      stop_id VARCHAR NOT NULL,
      ticketing_stop_id VARCHAR NOT NULL,
      agency_id VARCHAR NOT NULL
    )""",
    sql"DROP table ticketing_identifier",
    """INSERT INTO ticketing_identifier (
      stop_id,
      ticketing_stop_id,
      agency_id
    ) values (?, ?, ?)"""
  )

  object Transfer extends Table(
    sql"""CREATE TABLE IF NOT EXISTS transfer (
      from_stop_id VARCHAR NOT NULL,
      to_stop_id VARCHAR NOT NULL,
      transfer_type VARCHAR NOT NULL,
      min_transfer_time VARCHAR NOT NULL
    )""",
    sql"DROP table transfer",
    """INSERT INTO transfer (
      from_stop_id,
      to_stop_id,
      transfer_type,
      min_transfer_time
    ) values (?, ?, ?, ?)"""
  )

  object Trip extends Table(
    sql"""CREATE TABLE IF NOT EXISTS trip (
      route_id VARCHAR NOT NULL,
      service_id VARCHAR NOT NULL,
      trip_id VARCHAR NOT NULL,
      trip_headsign VARCHAR,
      trip_short_name VARCHAR,
      direction_id VARCHAR,
      block_id VARCHAR,
      shape_id VARCHAR,
      wheelchair_accessible VARCHAR,
      bikes_allowed VARCHAR
    )""",
    sql"DROP table trip",
    """INSERT INTO trip (
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
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

abstract case class Table(
  create: Fragment,
  drop: Fragment,
  insertInto: String
)
