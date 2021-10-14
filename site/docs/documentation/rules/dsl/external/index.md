---
layout: docs
title: External DSL
---

* Contents
{:toc}

# The external DSL

[![Maven Central](https://img.shields.io/maven-central/v/com.mobimeo/fs2-gtfs-rules-syntax_2.13.svg)](https://mvnrepository.com/artifact/com.mobimeo/fs2-gtfs-rules-syntax_2.13)

To use the GTFS rule syntax, add the dependency to your build file. For instance for sbt:

```scala
libraryDependencies += "com.mobimeo" %% "fs2-gtfs-rules-syntax" % "<version>"
```

It is cross compiled for scala 2.13 and scala 3.

The external DSL allows you to write rules in a declarative way without any knowledge of scala. This can be useful if you intend to have non developer to write the rules, or if you want to store them in a text file or database, without needing to compile scala code when they are changed.

## The DSL syntax

The DSL syntax follows the following grammar.

```ruby
file = ruleset*

ruleset = 'ruleset' 'for' filename '{' rule ('or' rule)* '}'

filename = 'agency'
         | 'stops'
         | 'routes'
         | 'trips'
         | 'stop_times'
         | 'calendar'
         | 'calendar_dates'
         | 'fare_attributes'
         | 'fare_rules'
         | 'shapes'
         | 'frequencies'
         | 'transfers'
         | 'pathways'
         | 'levels'
         | 'feed_info'
         | 'translations'
         | 'attributions'

rule = 'rule' ident '{' 'when' matcher 'then' action '}'

matcher = '(' matcher ')'
        | '!' matcher
        | 'any'
        | variable 'exists'
        | value valueMatcher
        | matcher 'and' matcher
        | matcher 'or' matcher

valueMatcher = '==' value
             | 'in' '[' value* ']'
             | 'matches' regex

action = 'delete'
       | transformation ('then' transformation)*
       | log

transformation = 'set' 'row' '[' str ']' 'to' value
               | 'search' regex 'in' 'row' '[' str ']' 'and' 'replace' 'by' value

log = 'debug' '(' expr ')'
    | 'info' '(' expr ')'
    | 'warning' '(' expr ')'
    | 'error' '(' expr ')'

expr = ident '(' (expr (',' expr)*)? ')'
     | value
     | expr + expr

value = str
      | variable

variable = 'row' '[' value ']'
         | 'ctx' '[' value ']' ('[' value ']')*

ident = [a-zA-Z] [a-zA-Z0-9_-]*
str = '"' <any character with " and \ escaped> '"'
regex = '/' <any character with / and \ escaped> '/'
```

Matcher operators have the following precedence: `!` has precedence over `and` which as precedence over `or`. This means that `!m1 and m2 or m3` is equivalent to `((!m1) and m2) or m3`.

Combinators are left associative, i.e. `m1 and m2 and m3` is equivalent to `(m1 and m2) and m3`.

## Parsing the DSL

There are two way to parse rules written with the external DSL:
 1. Using the `RuleParser` combinators from the `com.mobimeo.gtfs.rules.syntax` package.
 2. Using the string interpolators provided in the `com.mobimeo.gtfs.rules.syntax.lexicals` package.

**Note:** Rules parsed with the external DSL are effect free, only rules with [`Id`][cats-id] are generated. The effectful rules only come from the ones using anonymous scala functions, which cannot be expressed with the DSL.

### Using the `RuleParser` class directly

The parser is based on [cats-parse][cats-parse] and exposes different entry points to parse
 - a `file` which consists of zero or more rule sets,
 - a `ruleset` which defines a single set of rules,
 - a `rule` which defines a single rule.

For instance, you can express the rule that makes stop names uppercase as follows:

```scala mdoc
import com.mobimeo.gtfs.rules.syntax._

RuleParser.ruleset.parseAll("""ruleset for stops {
                              |  rule uppercase-stops {
                              |    when row["stop_name"] exists
                              |    then set row["stop_name"] to uppercase(row["stop_name"])
                              |  }
                              |}""".stripMargin)
```

#### Error reporting

`cats-parse` does not (yet) provide a nice pretty printing for parse errors, but we provide a module that can be used to render them nicely, namely the `Prettyprint` class.

```scala mdoc
import cats.syntax.all._

val input = """rule wrong-uppercase-stops {
              |  when row["stop_name"] exist
              |  then set row["stop_name"] to uppercase(row["stop_name"])
              |}""".stripMargin

RuleParser.rule.parseAll(input).leftMap(error => new Prettyprint(input).prettyprint(error))
```

### Using the interpolators

When the rules you want to parse are known at compile time and you would like to be notified of syntax errors at compile time, you can use the provided interpolators from package `com.mobimeo.gtfs.rules.syntax.literals`.
The rules defined with these literals are parsed and checked at compile time, and they return the rules themselves, no wrapped in any `Either`.

```scala mdoc
import com.mobimeo.gtfs.rules.syntax.literals._

val rules =
  ruleset"""ruleset for stops {
              rule uppercase-stops {
                when row["stop_name"] exists
                then set row["stop_name"] to uppercase(row["stop_name"])
              }
           }"""
```

#### Error reporting

Since rules are parsed at compile time, error reporting occurs when compiling the scala code in the scala compiler.

```scala mdoc:fail
rule"""rule wrong-uppercase-stops {
         when row["stop_name"] exist
         then set row["stop_name"] to uppercase(row["stop_name"])
       }"""
```

The code snippet above does not compile at all.

## Pretty printing

If you have set of rules that you'd like to serialize to the concrete syntax, you can use the `Show` instances available in the `com.mobimeo.gtfs.rules.syntax.pretty` package.

```scala mdoc
import com.mobimeo.gtfs.rules.syntax.pretty._

rules.show
```

**Note:** the concrete syntax does not allow for expressing anonymous functions. If you try to serialize rules containing calls to anonymous functions, it will be rendered as concatenation of the arguments.

```scala mdoc
import cats.Id
import com.mobimeo.gtfs.rules._

val fun = (s: List[String]) => s.mkString("-")

val t: Expr[Id] = Expr.AnonymousFunction[Id](fun,
  List(
    Expr.Val(Value.Str("a")),
    Expr.Val(Value.Str("b")),
    Expr.Val(Value.Str("c"))))

t.show
```

[cats-parse]: https://github.com/typelevel/cats-parse
[cats-id]: https://typelevel.org/cats/api/cats/index.html#Id[A]=A
