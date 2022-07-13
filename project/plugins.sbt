// strict compiler flags
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.3")
// source code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
// coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
// check dependencies against OWASP vulnerabilities
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "4.0.0")
// check and add missing license headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.7.0")
// manage github actions from build definition
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
// manage the documentation website
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.3.4")
// generate unified documentation
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
// release automatically
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
