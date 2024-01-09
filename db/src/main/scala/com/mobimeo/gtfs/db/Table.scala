package com.mobimeo.gtfs.db

import doobie.*
import doobie.implicits.*

object Table:
  object Agency extends Table(
    sql"""CREATE TABLE agency (
      id VARCHAR NOT NULL UNIQUE,
      name VARCHAR NOT NULL,
      url VARCHAR,
      timezone VARCHAR NOT NULL,
      lang VARCHAR NOT NULL,
      ticketing_deep_link_id VARCHAR
    )""",
    "INSERT INTO agency (id, name, url, timezone, lang, ticketing_deep_link_id) values (?, ?, ?, ?, ?, ?)"
  )

  object CalendarDate extends Table(
    sql"""CREATE TABLE calendar_date (
      service_id VARCHAR NOT NULL,
      date VARCHAR NOT NULL,
      exception_type VARCHAR NOT NULL
    )""",
    "INSERT INTO calendar_date (service_id, date, exception_type) values (?, ?, ?)"
  )

  object FeedInfo extends Table(
    sql"""CREATE TABLE feed_info (
      feed_version VARCHAR NOT NULL,
      feed_publisher_name VARCHAR NOT NULL,
      feed_publisher_url VARCHAR NOT NULL,
      feed_lang VARCHAR NOT NULL,
      default_lang VARCHAR NOT NULL,
      feed_start_date VARCHAR NOT NULL,
      feed_end_date VARCHAR NOT NULL,
      feed_contact_email VARCHAR NOT NULL,
      feed_contact_url VARCHAR NOT NULL
    )""",
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
    sql"""CREATE TABLE route (
      route_id VARCHAR NOT NULL,
      agency_id VARCHAR NOT NULL,
      route_short_name VARCHAR NOT NULL,
      route_long_name VARCHAR NOT NULL,
      route_type VARCHAR NOT NULL,
      route_color VARCHAR NOT NULL,
      route_text_color VARCHAR NOT NULL
    )""",
    """INSERT INTO route (
      route_id,
      agency_id,
      route_short_name,
      route_long_name,
      route_type,
      route_color,
      route_text_color
    ) values (?, ?, ?, ?, ?, ?, ?)"""
  )

  object StopTime extends Table(
    sql"""CREATE TABLE stop_time (
      trip_id VARCHAR NOT NULL,
      arrival_time VARCHAR NOT NULL,
      departure_time VARCHAR NOT NULL,
      stop_id VARCHAR NOT NULL,
      stop_sequence VARCHAR NOT NULL,
      stop_headsign VARCHAR NOT NULL
    )""",
    """INSERT INTO stop_time (
      trip_id,
      arrival_time,
      departure_time,
      stop_id,
      stop_sequence,
      stop_headsign
    ) values (?, ?, ?, ?, ?, ?)"""
  )

  object Stop extends Table(
    sql"""CREATE TABLE stop (
      stop_id VARCHAR NOT NULL,
      stop_name VARCHAR NOT NULL,
      stop_timezone VARCHAR NOT NULL,
      stop_lat VARCHAR NOT NULL,
      stop_lon VARCHAR NOT NULL,
      location_type VARCHAR NOT NULL,
      parent_station VARCHAR NOT NULL,
      platform_code VARCHAR NOT NULL,
      wheelchair_boarding VARCHAR NOT NULL,
      stop_code VARCHAR NOT NULL
    )""",
    """INSERT INTO stop (
      stop_id,
      stop_name,
      stop_timezone,
      stop_lat,
      stop_lon,
      location_type,
      parent_station,
      platform_code,
      wheelchair_boarding,
      stop_code
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
  )

  object TicketingIdentifier extends Table(
    sql"""CREATE TABLE ticketing_identifier (
      stop_id VARCHAR NOT NULL,
      ticketing_stop_id VARCHAR NOT NULL,
      agency_id VARCHAR NOT NULL
    )""",
    """INSERT INTO ticketing_identifier (
      stop_id,
      ticketing_stop_id,
      agency_id
    ) values (?, ?, ?)"""
  )

  object Transfer extends Table(
    sql"""CREATE TABLE transfer (
      from_stop_id VARCHAR NOT NULL,
      to_stop_id VARCHAR NOT NULL,
      transfer_type VARCHAR NOT NULL,
      min_transfer_time VARCHAR NOT NULL
    )""",
    """INSERT INTO transfer (
      from_stop_id,
      to_stop_id,
      transfer_type,
      min_transfer_time
    ) values (?, ?, ?, ?)"""
  )

  object Trip extends Table(
    sql"""CREATE TABLE trip (
      route_id VARCHAR NOT NULL,
      service_id VARCHAR NOT NULL,
      trip_id VARCHAR NOT NULL,
      trip_short_name VARCHAR NOT NULL,
      block_id VARCHAR NOT NULL,
      wheelchair_accessible VARCHAR NOT NULL,
      bikes_allowed VARCHAR NOT NULL
    )""",
    """INSERT INTO trip (
      route_id,
      service_id,
      trip_id,
      trip_short_name,
      block_id,
      wheelchair_accessible,
      bikes_allowed
    ) values (?, ?, ?, ?, ?, ?, ?)"""
  )

abstract case class Table(
  createTable: Fragment,
  insert: String
)
