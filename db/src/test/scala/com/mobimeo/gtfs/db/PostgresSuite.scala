package com.mobimeo.gtfs.db

import cats.effect.*
import doobie.*
import doobie.util.transactor.Transactor
import weaver.*

abstract class PostgresSuite extends IOSuite {
  override type Res = Transactor[IO]
  override def sharedResource: Resource[IO, Res] = Resource {
    for
      config <- DatabaseConfig.apply
      xa      = Transactor.fromDriverManager[IO](config.driver, config.url, config.username, config.password, logHandler = None)
      create <- Schema.create(xa).as(xa)
      drop    = Schema.drop(xa)
    yield create -> drop
  }
}
