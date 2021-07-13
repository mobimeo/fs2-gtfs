// === Common settings used by all modules ===
val commonSettings = Seq(
  organization := "com.mobimeo",
  cancelable in Global := true,
  headerLicense := Some(HeaderLicense.ALv2("2021", "Mobimeo GmbH")),
  libraryDependencies ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
    case Some((2, _)) =>
      List(
        compilerPlugin("org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full),
        compilerPlugin("com.olegpy" % "better-monadic-for" % "0.3.1" cross CrossVersion.binary)
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
val scala3 = "3.0.0"

ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := List(scala213, scala3)

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
  .settings(name := "fs2-gtfs-core", libraryDependencies ++= Dependencies.core ++ PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
    case Some((2, _)) => Dependencies.coreScala2
  }
    .toList
    .flatten)

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings)
  .settings(noPublish)
  .dependsOn(core)
