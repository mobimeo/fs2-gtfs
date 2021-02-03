import sbt._

object Versions {
  val fs2        = "2.5.0"
  val fs2Data    = "0.9.0"
  val enumeratum = "1.6.1"
}

object Dependencies {

  val core = List(
    "co.fs2"       %% "fs2-io"               % Versions.fs2,
    "org.gnieh"    %% "fs2-data-csv"         % Versions.fs2Data,
    "org.gnieh"    %% "fs2-data-csv-generic" % Versions.fs2Data,
    "com.beachape" %% "enumeratum"           % Versions.enumeratum
  )

}
