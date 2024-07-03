---
layout: docs
title: Reading from a GTFS file
---

* Contents
{:toc}

# Reading from a GTFS file

The GTFS standard defines the format in which a GTFS file is shared. It consists in a bunch of CSV files within a zip file.

```scala mdoc
import com.mobimeo.gtfs.file._
import com.mobimeo.gtfs.model._

import cats.effect._
import cats.effect.unsafe.implicits.global

import fs2.io.file.Path

val gtfs = GtfsFile[IO](Path("site/gtfs.zip"))
```

The acquired GTFS resource gives access to the content under the `read` namespace. The content is streamed entity by entity. This way the files are never entirely loaded into memory when reading them. The `read` namespace exposes function to read from the standard files, for instance if one wants to read the available route names from a GTFS file, on can use the `routes` function as follows. Note that it uses the [provided data model][gtfs-model].

```scala mdoc
gtfs.use { gtfs =>
  gtfs.read
    .routes[Route].collect {
      case Route(id, _, Some(name), _, _, _, _, _, _, _) => s"$name ($id)"
    }
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain
}.unsafeRunSync()
```

The `read` namespace contains shortcuts to read entities from the standard files. You need to provide the type you want to decode the entities to Route.
You can provide your own type, provided that you also provide a [`CsvRowDecoder`][csv-row-decoder] for that type.

For instance if you are only interested in extracting route name and identifier, you can define you own data model for these two fields.

```scala mdoc
import fs2.data.csv.CsvRowDecoder
import fs2.data.csv.generic.CsvName
import fs2.data.csv.generic.semiauto._

case class IdNameRoute(
  @CsvName("route_id") id: String,
  @CsvName("route_short_name") name: Option[String])
object IdNameRoute {
  implicit val decoder: CsvRowDecoder[IdNameRoute, String] = deriveCsvRowDecoder
}

gtfs.use { gtfs =>
  gtfs.read
    .routes[IdNameRoute].collect {
      case IdNameRoute(id, Some(name)) => s"$name ($id)"
    }
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain
}.unsafeRunSync()
```

The simplest way to get the proper decoder for your own case classes is to use the [fs2-data `generic` module][fs2-data-generic] as shown in the example above.

## Non standard files

If you want to access files that are not part of the GTFS standard, you can use the `file` function, which takes the file name.

**Note:** The file has to be a valid CSV file.

For instance, to access a `contributors.txt` file that would list the contributors of the file, you can use this function.

```scala mdoc
case class Contributor(name: String, email: String)
object Contributor {
  implicit val decoder: CsvRowDecoder[Contributor, String] = deriveCsvRowDecoder
}

gtfs.use { gtfs =>
  gtfs.read
    .file[Contributor]("contributors.txt").map {
      case Contributor(name, email) => s"$name ($email)"
    }
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain
}.unsafeRunSync()
```

## Raw rows

For some usage, you might not want to deserialize the rows to a typed data model, but want to work with raw CSV rows from the files. This is useful for instance in case you want to modify the values of a field without validating or needing to know what the other fields contain.
The `GtfsFile` class provides a `raw` variant for every file access. For instance, if you want to extract the route names without deserializing, you can use this approach.

```scala mdoc"
gtfs.use { gtfs =>
  gtfs.read
    .rawRoutes
    .map(s => s("route_short_name"))
    .unNone
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain
}.unsafeRunSync()
```

[gtfs-model]: ../../model/
[csv-row-decoder]: https://fs2-data.gnieh.org/documentation/csv/#csvrowdecoder--csvrowencoder
[fs2-data-generic]: https://fs2-data.gnieh.org/documentation/csv/generic/
