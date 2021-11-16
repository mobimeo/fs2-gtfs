---
layout: docs
title: Embedded DSL
---

* Contents
{:toc}

# The embedded DSL

The embedded DSL allows you to write scala code to type-safely create rule sets in a convenient way.

Rules are grouped in rule sets, applying to a specific file in GTFS data. The DSL helps you write your rules in scala in an easy and typesafe way.

## Define a rule set

To define a set of rule, you need to have an instance of the `Dsl` class for you effect type. By importing members of this class, you bring in scope the facilities.

```scala mdoc
import com.mobimeo.gtfs._
import com.mobimeo.gtfs.rules._

import cats.effect._

val dsl = new Dsl[IO]

import dsl._
```

Rule sets are defined within the `ruleset` method. The first parameter defines which file the set applies to within the GTFS data, the second part defines the rules included in the rule set.

The different rules will be tried in order, and the first matching one will be selected for each row in the file.

To write several rules, you can separate them with the `orElse` operator.

```scala mdoc:compile-only
val someRule: RulesBuilder = ???
val someOtherRule: RulesBuilder = ???

ruleset(StandardName.Routes) {
  someRule orElse someOtherRule
}
```

## Access to the current row and context

Within the definition of a rule (matcher and transformations) it might come in handy to access the content of the current row. This can be achieved by using the `row` variable available in the DSL.

For instance to access the `route_short_name` field of the current row (if it exists):
```scala mdoc
row("route_short_name")
```

Sometimes, not only the current row value is interesting but we might want to use some sort of global context, containing values.
The DSL provides the `ctx` variable, which gives you access to the hierarchical context, which consists of a tree of string. Accessing values can be done by providing the path to the leaf.

For instance, let's say you have the current date in the global context as follows:
```json
{
  "meta": {
    "date": "2021-01-01"
  }
}
```

You can access the `date` field like this:
```scala mdoc
ctx("meta")("date")
```

Both `row` and `ctx` can be used in the matcher part of the rule and the transformation one.

## The row matcher

The first part of a rule is the matcher. The matcher defines which rows are eligible for performing the actions of the rule.

**Note:** Matcher is a pure expression, it cannot perform any side effect, that is why it is not possible to call functions in a matcher.

The matcher expressions are usually based on the content of the current row.
To check taht a row contains a field, you can use the `exists` operator:
```scala mdoc
row("stop_name").exists
```

Values can be compared using the `===` operator:
```scala mdoc
row("stop_name") === "Some Stop"
```

You can also check that a value is withing a list of values using the `in` operator:
```scala mdoc
row("stop_name") in List("Some Stop", "Some Other Stop")
```

You can check for some regular expression patterns using the `matches` operator:
```scala mdoc
row("stop_name") matches "^Berlin, .*"
```

Matchers can be combined together via `and`, `or`, and `!` operators to create a new one:

```scala mdoc
row("stop_name").exists and !row("stop_id").exists
```

## Perform transformations

One possible kind of rules are the rules that perform transformations on the matched rows.
We can for instance define the rules to make all stop names uppercase as follows:

```scala mdoc
ruleset(StandardName.Stops) {
  rule("uppercase-stops")
    .when(any)
    .perform(row("stop_name") := uppercase(row("stop_name")))
}
```

The previous transformation sets the row field value to a new value. Another possible transformation is to search for a pattern and replace it by a new value.

For instance, if we want to remove the leading `Berlin, ` occurrences in stop names:
```scala mdoc
ruleset(StandardName.Stops) {
  rule("uppercase-stops")
    .when(row("stop_name") matches "^Berlin, .*$")
    .perform(in("stop_name").search("^Berlin, ").andReplaceBy(""))
}
```

In the replacement string, `$n` refers to the value of the n-th capturing group in the regular expression for n between 0 and 9.

The `perform` function takes several transformations (at least one) which will be applied in order of defnition. A transformation is performed on the result of the previous ones.

## Delete a row

The second action a rule can perform is deleting matching rows.

For instance if a stop was closed but still appears in GTFS data, you could add this rule:

```scala mdoc
ruleset(StandardName.Stops) {
  rule("delete-französische-straße")
    .when(row("route_short_name") === "Französische Straße")
    .delete
}
```

## Log data

The last possible action to perform for a rule is logging. The logging action will be executed for the matching rows only. Rows (matching or not) are always left unchanged for logging rules.

For instance a rule set that logs missing colors for routes could look like this:
```scala mdoc:height=80
ruleset(StandardName.Routes) {
  rule("log-missing-colors")
    .when(!row("route_color").exists and !row("route_text_color").exists)
    .error(concat"Route ${row("route_short_name")} (${row("route_id")}) is missing all colors") orElse
  rule("log-missing-line-color")
    .when(!row("route_color").exists)
    .error(concat"Route ${row("route_short_name")} (${row("route_id")}) is missing line color") orElse
  rule("log-missing-text-color")
    .when(!row("route_color").exists)
    .warning(concat"Route ${row("route_short_name")} (${row("route_id")}) is missing text color")
}
```

## Call anonymous functions

In addition to calling functions by name, you can use the DSL to call standard scala function. Any function with signature `List[String] => F[String]` can be called.

```scala mdoc
def makeKebab(args: List[String]) = IO.pure(args.mkString("-"))

call(makeKebab _)("a", "b", "c")
```
