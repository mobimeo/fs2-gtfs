# `fs2-gtfs`

This project is a [GTFS][gtfs] processing library based on [fs2][fs2].
It consists of several API levels, and can be used to perform several kind of tasks on GTFS data.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [`fs2-gtfs`](#fs2-gtfs)
  - [Running the examples](#running-the-examples)
  - [Basic usage](#basic-usage)
    - [Reading GTFS files](#reading-gtfs-files)
    - [Writing GTFS files](#writing-gtfs-files)
  - [GTFS data model](#gtfs-data-model)
    - [GTFS Routes](#gtfs-routes)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Running the examples

The [`examples` directory][examples-dir] contains a bunch of usage examples which can be run from [sbt][sbt]. At the root of this project run `sbt` to start an interactive session, and then:

```shell
sbt:gtfs-lib> examples/run
```

And select the example to be run. If arguments are required just add them to the command. For instance:

```shell
sbt:gtfs-lib> examples/run /path/to/gtfs.zip
```

## Basic usage

At its `core` this library is based on the `Gtfs` class, which gives access to `Pipe`s for reading and writing content of the GTFS files.

Both reading and writing `Pipe`s come in two flavours:
 1. A _raw_ version, which operates on raw CSV rows, i.e. non empty lists of strings.
 2. A _parsed_  version, which allows for passing a type (typically a case class) into which the rows are decoded. This allows for handling higher level types (see below).

The `Gtfs` class exposes two namespaces, named `read` and `write`, each of which contains the pipes for accessing the different GTFS files.

### Reading GTFS files

For instance, if you want to gather all stop names from a GTFS file, you can use this snippet:

```scala
import cats.effect._
import com.mobimeo.gtfs._
import java.nio.file.Paths

implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

Blocker[IO]
  .use { blocker =>
    Gtfs[IO](Paths.get("/path/to/gtfs.zip"), blocker).use { gtfs =>
      gtfs.read.rawStops.map(s => s("stop_name").filter(_.nonEmpty)).unNone.compile.toList
    }
  }
```

This makes use of the _raw_ API, which results in a stream of `CsvRow`, allowing to access values given a column name. The call to `unNone` removes the `None` elements from the stream (i.e. stops with no name).

You can see the runnable example in the [`examples` directory][examples-dir]. Example is named `ListStopNames`

### Writing GTFS files

Writing into a GTFS file, can be done using the `Pipe`s in `Gtfs.write`. Their signature is the counterpart of the reading ones from the `Gtfs` class.
A typical use is to process data from a GTFS file and save the processed result into a new file.

For instance, if you want to process stop names to put them in all caps, and save the result in a new GTFS file, you can use this pattern:

```scala
import cats.effect._
import com.mobimeo.gtfs._
import java.nio.file.Paths

implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

Blocker[IO]
  .use { blocker =>
    Gtfs[IO](Paths.get("/path/to/gtfs.zip"), blocker).use { gtfs =>
      gtfs.copyTo(Paths.get("/path/to/gtfs-caps.zip")).use { target =>
        gtfs.read.rawStops
          .map(_.modify("stop_name")(_.toUpperCase))
          .through(target.write.rawStops)
          .compile
          .drain
      }
    }
  }
```

This example can be run as example `AllCapsStops`.

## GTFS data model

The library also provides a higher level view on the GTFS file, by providing case classes representing the standard file content. You can use it to access a typed version of the GTFS file.
The classes in the `com.mobimeo.gtfs.model` package implement the standard columns and disregard any extra column.

We can rewrite the snippet listing stop names as follows:
```scala
import cats.effect._
import com.mobimeo.gtfs._
import com.mobimeo.gtfs.model._
import java.nio.file.Paths

implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

Blocker[IO]
  .use { blocker =>
    Gtfs[IO](Paths.get("/path/to/gtfs.zip"), blocker).use { gtfs =>
      gtfs.read.stops[Stop].map(_.name.filter(_.nonEmpty)).unNone.compile.toList
    }
  }
```

In the `examples` directory, this is the example named `ListStopNamesTyped`.

Notice that the model class is given as a type parameter to the `stops` method. You can provide any type here as long as there is an instance of [`CsvRowDecoder`][fs2-data-csv-decoder].
This allows for providing custom model type in case:
 - your GTFS files uses non standard columns you are interested in;
 - or you only want to extract some columns.

For instance in our example, we are only interested in stop names, and could decide to use a type that only contains a `name` field.
```scala
import cats.effect._
import com.mobimeo.gtfs._
import com.mobimeo.gtfs.model._
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._
import java.nio.file.Paths

case class StopName(name: Option[String])
object StopName {
  implicit val decoder: CsvRowDecoder[StopName, String] = deriveDecoder
}

implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

Blocker[IO]
  .use { blocker =>
    Gtfs[IO](Paths.get("/path/to/gtfs.zip"), blocker).use { gtfs =>
      gtfs.read.stops[StopName].collect { case StopName(Some(name)) => name }.compile.toList
    }
  }
```

### GTFS Routes

Routes have a field indicating their type. The GTFS standard defines only 13 of them, but there is an extension for more fine-grained route types.
The default model provided by the library allows for parameterizing routes with the route type type. One can use `Int` to get the raw type, but it also defines enumerations for types.
Depending on the type used by the GTFS files, you can read routes using:
```scala
// use this if your GTFS file uses the simple GTFS route types. This is an alias for `Route[SimpleRouteType]`
gtfs.read.routes[SimpleRoute]
// use this if your GTFS file uses the extended GTFS route types. This is an alias for `Route[ExtendedRouteType]`
gtfs.read.routes[ExtendedRoute]
// use this if your GTFS file uses both the simple and the extended GTFS route types. This is an alias for `Route[Either[SimpleRouteType, ExtendedRouteType]]`
gtfs.read.routes[EitherRoute]
```

[gtfs]: https://developers.google.com/transit/gtfs/reference/
[fs2]: https://fs2.io
[examples-dir]: /examples/
[sbt]: https://www.scala-sbt.org/
[fs2-data-csv-decoder]: https://fs2-data.gnieh.org/documentation/csv/#csvrowdecoder--csvrowencoder
