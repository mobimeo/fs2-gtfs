---
layout: docs
title: Data Model
---

# The GTFS data model

The core library provides case classes encoding the entity as described in the [GTFS standard][gtfs]. These classes can be used to read and write GTFS data when you are dealing with files respecting the standard.
The entities are typed and can be used safely for transformations.

## GTFS Routes

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
