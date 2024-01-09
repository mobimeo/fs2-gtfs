package com.mobimeo.gtfs.db

import cats.effect.*
import com.mobimeo.gtfs.file.GtfsFile
import doobie.*
import doobie.h2.*
import fs2.io.file.*
import weaver.*

abstract class InMemoryH2Suite extends IOSuite {
  private val resource = getClass.getResource("simple-gtfs.zip")
  private val jdbcUrl   = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

  override type Res = (H2Transactor[IO], GtfsFile[IO])
  override def sharedResource: Resource[IO, Res] = {
    val xa = for
      ce       <- ExecutionContexts.fixedThreadPool[IO](4)
      xa       <- H2Transactor.newH2Transactor[IO](jdbcUrl, "sa", "", ce)
      _        <- Resource(Schema.create(xa).map(_ -> IO.unit))
    yield xa
    Resource.both(xa, GtfsFile.fromClasspath(resource))
 }
}
