package com.mobimeo.gtfs.db

import doobie.*
import doobie.implicits.*
import weaver.*

object SchemaSpec extends InMemoryH2Suite {
  test("The database schema contains all relevant tables") { case (xa, _) =>
    for
      tables <- sql"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'"
                  .query[String]
                  .to[Set]
                  .transact(xa)
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
