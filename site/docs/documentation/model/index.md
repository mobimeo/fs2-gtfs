---
layout: docs
title: Data Model
---

# The GTFS data model

The core library provides case classes encoding the entity as described in the [GTFS standard][gtfs]. These classes can be used to read and write GTFS data when you are dealing with files respecting the standard.
The entities are typed and can be used safely for transformations.

## GTFS Routes

Routes use Google's extended route types. The GTFS standard defines only 13 of them.
One can use `Int` to get the raw type, but it also defines enumerations for types.

[gtfs]: https://developers.google.com/transit/gtfs/reference/
