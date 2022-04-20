// strict compiler flags
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
// source code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
// coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")
// check dependencies against OWASP vulnerabilities
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "3.1.3")
// check and add missing license headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.5")
// manage github actions from build definition
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
// manage the documentation website
addSbtPlugin("com.47deg" % "sbt-microsites" % "1.3.4")
// generate unified documentation
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
// release automatically
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
