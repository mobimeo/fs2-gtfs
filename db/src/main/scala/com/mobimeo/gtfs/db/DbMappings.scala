package com.mobimeo.gtfs.db

import doobie._
import java.time.ZoneId

trait DbMappings {
  implicit val zoneIdGet: Get[ZoneId] = Get[String].tmap(ZoneId.of)
  implicit val zoneIdPut: Put[ZoneId] = Put[String].tcontramap(_.toString)
}
