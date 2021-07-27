---
layout: docs
title: Reading from a GTFS file
---
# Reading from a GTFS file

The GTFS standard defines the format in which a GTFS file is shared. It consists in a bunch of CSV files within a zip file.

```scala mdoc
import com.mobimeo.gtfs.file._
import com.mobimeo.gtfs.model._

import cats.effect._
import cats.effect.unsafe.implicits.global

import java.nio.file._

val gtfs = GtfsFile[IO](Paths.get("site/gtfs.zip"))
```

The acquired GTFS resource gives access to the content under the `read` namespace. The content is streamed entity by entity. This way the files are never entirely loaded into memory when reading them. The `read` namespace exposes function to read from the standard files, for instance if one wants to read the available route names from a GTFS file, on can use the `routes` function as follows. Notes that it uses the [provided data model][gtfs-model].

```scala mdoc
gtfs.use { gtfs =>
  gtfs.read
    .routes[Route[Int]].collect {
      case Route(id, _, Some(name), _, _, _, _, _, _, _) => s"$name ($id)"
    }
    .intersperse("\n")
    .evalMap(s => IO(print(s)))
    .compile
    .drain
}.unsafeRunSync()
```

[gtfs-model]: ../../
