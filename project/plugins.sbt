// strict compiler flags
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
// source code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
// coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")
// check dependencies against OWASP vulnerabilities
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "3.1.3")
// check and add missing license headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
