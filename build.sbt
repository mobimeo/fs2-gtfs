// === Common settings used by all modules ===
val commonSettings = Seq(
  organization                                            := "com.mobimeo",
  cancelable in Global                                    := true,
  headerLicense                                           := Some(HeaderLicense.ALv2("2021", "Mobimeo GmbH")),
  licenses += ("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage                                                := Some(url("https://github.com/mobimeo/fs2-gtfs")),
  developers := List(
    Developer(
      id = "mobimeo",
      name = "Mobimeo OSS Team",
      email = "opensource@mobimeo.com",
      url = url("https://github.com/mobimeo")
    )
  ),
  libraryDependencies ++= Dependencies.common ++ PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) { case Some((2, _)) =>
      List(
        compilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
        compilerPlugin("com.olegpy"    % "better-monadic-for" % "0.3.1" cross CrossVersion.binary)
      )
    }
    .toList
    .flatten,
  resolvers ++= Resolver.sonatypeOssRepos("public"),
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  testFrameworks += new TestFramework("weaver.framework.CatsEffect")
)

val noPublish = List(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false
)

// === CI/CD settings ===
val scala213 = "2.13.10"
val scala3   = "3.2.1"

ThisBuild / scalaVersion       := scala213
ThisBuild / crossScalaVersions := List(scala213, scala3)

// publishing
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

// don't use sbt thin client
ThisBuild / githubWorkflowUseSbtThinClient := false

// publish website

ThisBuild / githubWorkflowAddedJobs += WorkflowJob(
  id = "site",
  name = "Deploy site",
  needs = List("publish"),
  javas = (ThisBuild / githubWorkflowJavaVersions).value.toList,
  scalas = (ThisBuild / scalaVersion).value :: Nil,
  cond = "startsWith(github.ref, 'refs/tags/v')".some,
  steps = githubWorkflowGeneratedDownloadSteps.value.toList :+
    WorkflowStep.Sbt(
      List("site/makeMicrosite"),
      name = Some("Compile Website"),
      cond = Some(s"matrix.scala == '$scala213'")
    ) :+
    WorkflowStep.Use(
      UseRef.Public("peaceiris", "actions-gh-pages", "v3"),
      name = Some(s"Deploy site"),
      params = Map(
        "publish_dir"  -> "./site/target/site",
        "github_token" -> "${{ secrets.GITHUB_TOKEN }}"
      )
    )
)

// === aggregating root project ===
lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(noPublish)
  .settings(
    test     := {},
    testOnly := {}
  )
  .aggregate(core, rules, rulesSyntax)

// === The modules ===

lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val site = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(commonSettings)
  .settings(noPublish)
  .settings(
    test                          := {},
    testOnly                      := {},
    githubWorkflowArtifactUpload  := false,
    micrositeName                 := "fs2-gtfs Website",
    micrositeDescription          := "fs2 based GTFS processing library",
    micrositeBaseUrl              := "/fs2-gtfs",
    micrositeDocumentationUrl     := "/fs2-gtfs/documentation",
    micrositeAuthor               := "Mobimeo GmbH",
    micrositeOrganizationHomepage := "https://mobimeo.com",
    micrositeTwitter              := "@MobimeoMobility",
    micrositeGithubOwner          := "mobimeo",
    micrositeGithubRepo           := "fs2-gtfs",
    micrositeGitterChannel        := false,
    micrositeFooterText := Some(
      """Icons by Becris and Zulfa Mahendra from the <a href="https://thenounproject.com">Noun Project</a>"""
    ),
    autoAPIMappings                            := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core, rules, rulesSyntax),
    docsMappingsAPIDir                         := "api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
    libraryDependencies ++= Dependencies.site,
    tpolecatCiModeOptions ~= { opts => opts.filterNot(_ == ScalacOptions.fatalWarnings) },
    mdocExtraArguments           := Seq("--no-link-hygiene"),
    githubWorkflowArtifactUpload := false
  )
  .dependsOn(core, rules, rulesSyntax)

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
  .settings(
    name := "fs2-gtfs-core",
    libraryDependencies ++= Dependencies.core ++ PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) { case Some((2, _)) =>
        Dependencies.coreScala2
      }
      .toList
      .flatten
  )

lazy val rules = project
  .in(file("rules"))
  .settings(commonSettings)
  .settings(name := "fs2-gtfs-rules", libraryDependencies ++= Dependencies.rules)
  .dependsOn(core)

lazy val rulesSyntax = project
  .in(file("rules/syntax"))
  .settings(commonSettings)
  .settings(name := "fs2-gtfs-rules-syntax", libraryDependencies ++= Dependencies.rulesSyntax)
  .dependsOn(rules)
