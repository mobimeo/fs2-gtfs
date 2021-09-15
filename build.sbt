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

lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val site = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(commonSettings)
  .settings(noPublish)
  .settings(
    test := {},
    testOnly := {},
    micrositeName := "fs2-gtfs Website",
    micrositeDescription := "fs2 based GTFS processing library",
    micrositeDocumentationUrl := "/documentation",
    micrositeAuthor := "Mobimeo GmbH",
    micrositeOrganizationHomepage := "https://mobimeo.com",
    micrositeTwitter := "@MobimeoMobility",
    micrositeGithubOwner := "mobimeo",
    micrositeGithubRepo := "fs2-gtfs",
    micrositeGitterChannel := false,
    autoAPIMappings := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core),
    docsMappingsAPIDir := "api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
    mdocExtraArguments := Seq("--no-link-hygiene")
  )
  .dependsOn(core)

ThisBuild / githubWorkflowBuildPostamble ++= List(
  WorkflowStep.Sbt(
    List("site/mdoc"),
    name = Some("Compile Documentation"),
    cond = Some(s"matrix.scala == '$scala213'")
  )
)

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
