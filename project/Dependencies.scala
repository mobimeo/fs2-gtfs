import sbt._

object Versions {
  val fs2        = "3.11.0"
  val fs2Data    = "1.11.1"
  val enumeratum = "1.7.3"
  val weaver     = "0.8.4"
  val circe      = "0.14.8"
  val log4cats   = "2.7.0"
  val catsParse  = "1.0.0"
  val literally  = "1.2.0"
}

object Dependencies {

  val core = List(
    "co.fs2"    %% "fs2-io"               % Versions.fs2,
    "org.gnieh" %% "fs2-data-csv"         % Versions.fs2Data,
    "org.gnieh" %% "fs2-data-csv-generic" % Versions.fs2Data
  )

  val rules = List(
    "io.circe"      %% "circe-core"    % Versions.circe,
    "org.typelevel" %% "log4cats-core" % Versions.log4cats
  )

  val rulesSyntax =
    List("org.typelevel" %% "cats-parse" % Versions.catsParse, "org.typelevel" %% "literally" % Versions.literally)

  val site = List(
    "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats
  )

  val common = List("com.disneystreaming" %% "weaver-cats" % Versions.weaver % Test)

}
