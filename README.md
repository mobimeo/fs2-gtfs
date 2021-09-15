# `fs2-gtfs`

This project is a [GTFS][gtfs] processing library based on [fs2][fs2].
It consists of several API levels, and can be used to perform several kind of tasks on GTFS data.

The current modules are:
 - `core` the core features to process GTFS files
 - `rules` the business rule engine and DSL

To build and view the documentation website locally run:
```shell
$ sbt site/makeMicrosite
$ cd site/target/site
$ jekyll serve -b /fs2-gtfs
```

To publish the documentation website publicly, run:
```shell
$ sbt site/pushMicrosite
```

For more details, please head over to the [website][website].

[gtfs]: https://developers.google.com/transit/gtfs/reference/
[fs2]: https://fs2.io
[website]: https://mobimeo.github.io/fs2-gtfs/
