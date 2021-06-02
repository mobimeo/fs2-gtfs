val commonSettings = Seq(
  organization := "com.mobimeo",
  cancelable in Global := true,
  scalaVersion := "2.13.6",
  headerLicense := Some(HeaderLicense.ALv2("2021", "Mobimeo GmbH")),
  // Add the macro paradise compiler flag, so we can use circe's @JsonCodec macro annotations
  scalacOptions ++= Seq("-Ymacro-annotations"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
)

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

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(name := "fs2-gtfs-core", libraryDependencies ++= Dependencies.core)

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings)
  .settings(Compile / scalaSource := baseDirectory.value)
  .dependsOn(core)
