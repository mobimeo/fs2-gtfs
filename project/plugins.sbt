// strict compiler flags
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.13")
// source code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
// coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
// check dependencies against OWASP vulnerabilities
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "2.0.0")
// check and add missing license headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
