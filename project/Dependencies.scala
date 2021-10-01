import sbt._

object Versions {
  val fs2        = "3.1.3"
  val fs2Data    = "1.0.0"
  val enumeratum = "1.7.0"
}

object Dependencies {

  val core = List(
    "co.fs2"       %% "fs2-io"               % Versions.fs2,
    "org.gnieh"    %% "fs2-data-csv"         % Versions.fs2Data,
    "org.gnieh"    %% "fs2-data-csv-generic" % Versions.fs2Data
  )

  val coreScala2 = List(
    "com.beachape" %% "enumeratum"           % Versions.enumeratum
  )

}
