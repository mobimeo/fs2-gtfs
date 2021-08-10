---
layout: docs
title: Working with GTFS files
---

# Working with GTFS files

The core module provides a way to work with GTFS files as described by [the standard][gtfs-standard]. The file is accessed wrapped within a [`Resource`][cats-effect-resource] to ensure it is released properly when done working with it.
This API lives in the `com.mobimeo.gtfs.file` package

```scala mdoc
import com.mobimeo.gtfs.file._

import cats.effect._
import cats.effect.unsafe.implicits.global

import java.nio.file._

GtfsFile[IO](Paths.get("site/gtfs.zip")).use { gtfs =>
  IO.pure(s"Some work with the GTFS file at ${gtfs.file}")
}.unsafeRunSync()
```

Within the `use` scope you can use the `gtfs` reference to read from and write to the file.
To achieve this, have a look at the dedicated pages:
 - [Reading from a GTFS file](reading)
 - [Writing to a GTFS file](writing)

[gtfs-standard]: https://developers.google.com/transit/gtfs/reference/
[cats-effect-resource]: https://typelevel.org/cats-effect/docs/std/resource
