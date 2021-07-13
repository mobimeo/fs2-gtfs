// === Common settings used by all modules ===
val commonSettings = Seq(
  organization := "com.mobimeo",
  cancelable in Global := true,
  headerLicense := Some(HeaderLicense.ALv2("2021", "Mobimeo GmbH")),
  // Add the macro paradise compiler flag, so we can use circe's @JsonCodec macro annotations scalacOptions ++= PartialFunction
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, n)) if n >= 13 =>
        Seq(
          "-Ymacro-annotations"
        )
      case Some((3, _)) =>
        Seq("-Ykind-projector")
    }
    .toList
    .flatten,
  libraryDependencies ++=
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
        )
      case _ =>
        // if scala 2.13.0 or later, macro annotations merged into scala-reflect
        Nil
    }) ++ PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, _)) =>
          List(
            compilerPlugin("org.typelevel" % "kind-projector"     % "0.13.0" cross CrossVersion.full),
            compilerPlugin("com.olegpy"    % "better-monadic-for" % "0.3.1" cross CrossVersion.binary)
          )
      }
      .toList
      .flatten
)

val noPublish = List(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

// === CI/CD settings ===
val scala213 = "2.13.6"
val scala3   = "3.0.0"

ThisBuild / scalaVersion := scala213
// enumeratum has no support for scala3 yet, maybe we don't need it if we use scala3 enums?
// ThisBuild / crossScalaVersions := List(scala213, scala3)

// === aggregating root project ===
lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(noPublish)
  .settings(
    test := {},
    testOnly := {}
  )
  .aggregate(core, examples)

// === The modules ===

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(name := "fs2-gtfs-core", libraryDependencies ++= Dependencies.core)

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings)
  .settings(noPublish)
  .dependsOn(core)
