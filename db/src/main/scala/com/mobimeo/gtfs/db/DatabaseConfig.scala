package com.mobimeo.gtfs.db

import cats.effect.*
import pureconfig.*
import pureconfig.module.catseffect.syntax.*

object DatabaseConfig {
  def apply[F[_]: Sync]: F[DatabaseConfig] = ConfigSource.default.loadF()

  given ConfigReader[DatabaseConfig] =
    ConfigReader.forProduct4("driver", "url", "username", "password")(DatabaseConfig.apply)
}

case class DatabaseConfig(driver: String, url: String, username: String, password: String)
