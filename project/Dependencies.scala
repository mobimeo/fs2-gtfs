import sbt._

object Versions {
  val catsEffect   = "3.5.2"
  val catsParse    = "1.0.0"
  val circe        = "0.14.7"
  val doobie       = "1.0.0-RC5"
  val enumeratum   = "1.7.3"
  val fs2          = "3.10.2"
  val fs2Data      = "1.11.0"
  val literally    = "1.2.0"
  val log4cats     = "2.7.0"
  val postgisJdbc  = "2.5.1"
  val pureconfig   = "0.17.6"
  val scala3       = "3.3.1"
  val weaver       = "0.8.4"
}

object Dependencies {
  val catsEffect = Seq(
    "org.typelevel"          %% "cats-effect"             % Versions.catsEffect)

  val common     = Seq(
    "com.disneystreaming"    %% "weaver-cats"             % Versions.weaver     % Test)

  val core = Seq(
    "co.fs2"                 %% "fs2-io"                  % Versions.fs2,
    "org.gnieh"              %% "fs2-data-csv"            % Versions.fs2Data,
    "org.gnieh"              %% "fs2-data-csv-generic"    % Versions.fs2Data)

  val db = Seq(
    "org.tpolecat"           %% "doobie-core"             % Versions.doobie,
    "org.tpolecat"           %% "doobie-hikari"           % Versions.doobie,
    "org.tpolecat"           %% "doobie-postgres"         % Versions.doobie,
    "net.postgis"             % "postgis-jdbc"            % Versions.postgisJdbc)

  val fs2Data    = Seq(
    "org.gnieh"              %% "fs2-data-csv-generic"    % Versions.fs2Data)

  val pureconfig = Seq(
    "com.github.pureconfig"  %% "pureconfig-cats-effect"  % Versions.pureconfig)

  val rules = Seq(
    "io.circe"               %% "circe-core"              % Versions.circe,
    "org.typelevel"          %% "log4cats-core"           % Versions.log4cats)

  val rulesSyntax = Seq(
    "org.typelevel"          %% "cats-parse"              % Versions.catsParse,
    "org.typelevel"          %% "literally"               % Versions.literally)

  val site = Seq(
    "org.typelevel"          %% "log4cats-slf4j"          % Versions.log4cats)
}
