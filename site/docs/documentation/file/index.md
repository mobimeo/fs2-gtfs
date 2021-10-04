---
layout: docs
title: Working with GTFS files
---

* Contents
{:toc}

# Working with GTFS files

The core module provides a way to work with GTFS files as described by [the standard][gtfs-standard]. The file is accessed wrapped within a [`Resource`][cats-effect-resource] to ensure it is released properly when done working with it.
This API lives in the `com.mobimeo.gtfs.file` package

```scala mdoc
import com.mobimeo.gtfs.file._

import fs2.io.file.Path

import cats.effect._
import cats.effect.unsafe.implicits.global

GtfsFile[IO](Path("site/gtfs.zip")).use { gtfs =>
  IO.pure(s"Some work with the GTFS file at ${gtfs.file}")
}.unsafeRunSync()
```

Within the `use` scope you can use the `gtfs` reference to read from and write to the file.
To achieve this, have a look at the dedicated pages:
 - [Reading from a GTFS file](reading)
 - [Writing to a GTFS file](writing)

## Implementation details

The `GtfsFile` class is implemented in a way that doesn't require to load the entire GTFS file into memory. The zip file is mapped to a [`FileSystem`][filesystem] and files are accessed through this API, under the hood.
Reading a CSV file from the GTFS data, streams the content, by default only loading in memory what is necessary to process the current CSV row.

The class is also implemented in a way that makes it possible to modify files in place, even though it is recommended to work on a copy.

[gtfs-standard]: https://developers.google.com/transit/gtfs/reference/
[cats-effect-resource]: https://typelevel.org/cats-effect/docs/std/resource
[filesystem]: https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/nio/file/FileSystem.html
