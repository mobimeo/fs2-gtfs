// === Common settings used by all modules ===
val commonSettings = Seq(
  organization := "com.mobimeo",
  cancelable in Global := true,
  headerLicense := Some(HeaderLicense.ALv2("2021", "Mobimeo GmbH")),
  // Add the macro paradise compiler flag, so we can use circe's @JsonCodec macro annotations
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, n)) if n >= 13 =>
        Seq(
          "-Ymacro-annotations"
        )
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
    }),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.3" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

// === CI/CD settings ===
val scala212 = "2.12.13"
val scala213 = "2.13.4"

ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := List(scala212, scala213)

// === aggregating root project ===
lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    publish := {},
    publishLocal := {},
    test := {},
    testOnly := {},
    publishArtifact := false
  )
  .aggregate(core)

// === The modules ===

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(name := "fs2-gtfs-core", libraryDependencies ++= Dependencies.core)

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings)
  .settings(Compile / scalaSource := baseDirectory.value)
  .dependsOn(core)
