---
layout: docs
title: GTFS Rule Engine
---

* Contents
{:toc}

# GTFS rule engine

[![Maven Central](https://img.shields.io/maven-central/v/com.mobimeo/fs2-gtfs-rules_2.13.svg)](https://mvnrepository.com/artifact/com.mobimeo/fs2-gtfs-rules_2.13)

To use the GTFS rule engine, add the dependency to your build file. For instance for sbt:

```scala
libraryDependencies += "com.mobimeo" %% "fs2-gtfs-rules" % "<version>"
```

It is cross compiled for scala 2.13 and scala 3.

The GTFS rule engine provides a declarative way of describing checks and transformations of GTFS data. This is useful if you need to verify and normalize your GTFS data before using them (e.g. in a pre-processing pipeline).

The idea of this module is to provide a clear DSL to handle the data in a declarative way. To understand the rationale behind this module, you can read the [blog post series][blog] we published.

## The rules

The rules are grouped in sets. A rule set applies to a given file in the GTFS data (e.g. `routes.txt`) and defines a list of rules.

For each row in the file, the rules are tried in order. The first matching one is taken an its associated action is executed. If no rule matches for the current row, then the row is left unchanged.

**Note:** the semantics for rules in a rule set is similar to the on of cases in a pattern match. The order matters as only the first matching one gets selected.

Rules are composed of two parts:
 1. A matcher, which defines which rows this rule applies to.
 2. An action, which defines the action to perform when a row is selected by the matcher.

We can for instance define a rule set that makes the station name uppercase:

```scala mdoc
import com.mobimeo.gtfs._
import com.mobimeo.gtfs.rules._

import cats.effect._
import cats.data.NonEmptyList
import cats.syntax.all._


val rules =
  RuleSet(
    StandardName.Stops.entryName,
    List(
      Rule(
       "uppercase-stops",
        Matcher.Any,
        Action.Transform(
          NonEmptyList.one(
            Transformation.Set[IO](
              Value.Str("stop_name"),
              Expr.NamedFunction(
                "uppercase",
                List(Expr.Val(Value.Field(Value.Str("stop_name")))))))))),
    Nil)
```

As you can see, this is not the easiest way to define the rules, that's why the library also provides a [DSL][dsl] to help write them in a more readable way.

## Create the engine

The base class to know to run rules on your data is the `Engine` that lives in the `com.mobimeo.gtfs.rules` package.

```scala mdoc
import org.typelevel.log4cats.slf4j.Slf4jLogger

// this is unsafe in production code, please refer to the log4cats documentation
implicit val unsafeLogger = Slf4jLogger.getLogger[IO]

val engine = Engine[IO]
```

An engine can be reused with different sets of rules and GTFS files.

## Execute the rules

Once you have an engine and rules, you can apply them to GTFS data using the `process` function.

```scala mdoc
import cats.effect.unsafe.implicits.global

import com.mobimeo.gtfs.file.GtfsFile

import fs2.io.file._

val gtfs = GtfsFile[IO](Path("site/gtfs.zip"))

val modified = Path("site/modified-rules-gtfs.zip")
gtfs.use { src =>
  src.copyTo(modified, CopyFlags(CopyFlag.ReplaceExisting)).use { tgt =>
    engine.process(List(rules), src, tgt)
  }
}.unsafeRunSync()

def printStops(gtfs: GtfsFile[IO]) =
  gtfs.read
    .rawStops
    .map(s => s("stop_name"))
    .unNone
    .take(5)
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain

// original file
gtfs.use(printStops(_)).unsafeRunSync()

// modified file
GtfsFile[IO](modified).use(printStops(_)).unsafeRunSync()
```

All rule sets provided to the `process` function are run in order. You can have several rule set applying to the same file, all of them will be applied to the file in the order they are defined.

## Default functions

The library provides a set of standard functions you can use from the rules. These are available in `Interpreter.defaultFunctions`. The function available by default are:

```scala mdoc:passthrough
import DocumentedFunction.markdown._

println(Interpreter.defaultFunctions[Either[Throwable, *]]
         .map { case (name, f) => show"**$name**: $f" }
         .mkString("\n\n"))
```

[blog]: https://medium.com/mobimeo-technology/designing-a-gtfs-business-rule-engine-part-1-d455e6d6add
[dsl]: dsl/
