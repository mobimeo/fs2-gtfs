---
layout: docs
title: Writing to a GTFS file
---

* Contents
{:toc}

# Writing to a GTFS file

Similarly [reading GTFS file][gtfs-file-reading], one can write GTFS files easily using this library. You can either modify an existing file, or create a new one from scratch.
The writing pipes, live in the `write` namespace within the `GtfsFile` class. This namespace provides a handful of `Pipe`s which give access to standard GTFS files.

```scala mdoc
import com.mobimeo.gtfs.file._
import com.mobimeo.gtfs.model._

import cats.effect._
import cats.effect.unsafe.implicits.global

import java.nio.file._

val gtfs = GtfsFile[IO](Paths.get("site/gtfs.zip"))
```

## Modifying an existing file

Oftentimes you already have an existing file in your hands and you want to modify it. To this end, you can pipe the read stream into the corresponding pipe.

```scala mdoc:compile-only
gtfs.use { gtfs =>
  gtfs.read
    .rawStops
    .map(s => s.modify("stop_name")(_.toUpperCase))
    .through(gtfs.write.rawStops)
    .compile
    .drain
}
```

This code modifies the file in place, making all stop names uppercase. However this is usually not recommended as data are overwritten and original data are replaced.
One should prefer to work on a copy of the original file. The `GtfsFile` provides a way to do it conveniently.

```scala mdoc
val modified = Paths.get("site/modified-gtfs.zip")
gtfs.use { src =>
  src.copyTo(modified, List(StandardCopyOption.REPLACE_EXISTING)).use { tgt =>
    src.read
      .rawStops
      .map(s => s.modify("stop_name")(_.toUpperCase))
      .through(tgt.write.rawStops)
      .compile
      .drain
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

When using `copyTo` the entirety of the original GTFS file content is copied and only files that are written to are modified. The rest is identical to the original file (including potential non standard files).

## Creating a new file

If one wants to create a new file from scratch, one need to tell the file needs to be created when creating the GTFS resource. An empty GTFS file will be created, and files can be added to it by using the associated `write` pipes.

```scala mdoc
def makeStop(id: String, name: String) =
  Stop(id, None, Some(name), None, None, None, None, None, None, None, None, None, None, None)

val file = Paths.get("site/gtfs2.zip")
GtfsFile[IO](file, create = true).use { gtfs =>
  fs2.Stream.emits(List(makeStop("stop1", "Some Stop"), makeStop("stop2", "Some Other Stop")))
    .covary[IO]
    .through(gtfs.write.stops[Stop])
    .compile
    .drain
}.unsafeRunSync()

GtfsFile[IO](file).use(printStops(_)).unsafeRunSync()
```

[gtfs-file-reading]: ../reading/
