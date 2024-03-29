---
layout: docs
title: Documentation
---

Welcome to the `fs2-gtfs` documentation page. You will find here all the information about the library and its modules. Please refer to the menu on the left side.

# Core Module

[![Maven Central](https://img.shields.io/maven-central/v/com.mobimeo/fs2-gtfs-core_2.13.svg)](https://mvnrepository.com/artifact/com.mobimeo/fs2-gtfs-core_2.13)

To use the core module, add the dependency to your build file. For instance for sbt:

```scala
libraryDependencies += "com.mobimeo" %% "fs2-gtfs-core" % "<version>"
```

It is cross compiled for scala 2.13 and scala 3.

The core of this library is the `Gtfs` interface, that provides the generic API for wroking with GTFS files. The API is based on namespaces, grouping together the operators by responsibility.
The `Gtfs` interface provides the following namespaces:
 - `read` provides streaming read access to the content of the GTFS data
 - `write` provides streaming write access to the content of the GTFS data
 - `has` provides ways of checking whether a file exists in the GTFS data
 - `delete` provides functions to delete a file from the GTFS data

This interface provides the basis features for working with GTFS data, independently from where and how it is stored. Concrete implementations will provide this interface and might extend it with implementation specific functions. Have a look at the documented implementation to choose the one that fits your needs.
