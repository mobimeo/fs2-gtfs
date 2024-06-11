package com.mobimeo.gtfs.db

import doobie.*
import doobie.implicits.*
import weaver.*

object SchemaSpec extends PostgresSuite {
  test("The database schema contains all relevant tables") { xa =>
    val sql = sql"SELECT tablename FROM pg_catalog.pg_tables where schemaname = 'public'"

    val expected = Set(
        "agencies",
        "calendars",
        "calendar_dates",
        "feed_infos",
        "routes",
        "spatial_ref_sys",
        "stops",
        "stop_times",
        "providers",
        "transfers",
        "trips"
      )
    // val sql = sql"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
    for
      tables <- sql.query[String].to[Set].transact(xa)
    yield expect(tables.map(_.toLowerCase) == expected)
  }
}
