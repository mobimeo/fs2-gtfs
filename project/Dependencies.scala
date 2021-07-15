import sbt._

object Versions {
  val fs2        = "3.0.6"
  val fs2Data    = "1.0.0"
  val enumeratum = "1.7.0"
  val doobie     = "1.0.0-M5"
  val sqliteJdbc = "3.36.0.1"
}

object Dependencies {

  val core = List(
    "co.fs2"       %% "fs2-io"               % Versions.fs2,
    "org.gnieh"    %% "fs2-data-csv"         % Versions.fs2Data,
    "org.gnieh"    %% "fs2-data-csv-generic" % Versions.fs2Data,
    "com.beachape" %% "enumeratum"           % Versions.enumeratum
  )

  val db = List(
    "org.tpolecat" %% "doobie-core" % Versions.doobie,
    "org.tpolecat" %% "doobie-hikari" % Versions.doobie,
    "org.xerial"    % "sqlite-jdbc" % Versions.sqliteJdbc % Test
  )

}
