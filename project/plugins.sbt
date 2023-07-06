// Remove me as soon as all sbt plugins use scala-xml 2 and we got rid of the annoying errors
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

// strict compiler flags
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.3")
// source code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
// coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.7")
// check dependencies against OWASP vulnerabilities
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "5.1.0")
// check and add missing license headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.9.0")
// manage github actions from build definition
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
// manage the documentation website
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.4.3")
// generate unified documentation
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
// release automatically
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
