// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "Reading from a GTFS file",
      "url": "/fs2-gtfs/documentation/file/reading/",
      "content": "Reading from a GTFS file Non standard files Raw rows Reading from a GTFS file The GTFS standard defines the format in which a GTFS file is shared. It consists in a bunch of CSV files within a zip file. import com.mobimeo.gtfs.file._ import com.mobimeo.gtfs.model._ import cats.effect._ import cats.effect.unsafe.implicits.global import fs2.io.file.Path val gtfs = GtfsFile[IO](Path(\"site/gtfs.zip\")) // gtfs: Resource[IO, GtfsFile[IO]] = Bind( // source = Allocate( // resource = cats.effect.kernel.Resource$$$Lambda$16478/0x0000000804362840@5112a487 // ), // fs = cats.effect.kernel.Resource$$Lambda$16480/0x0000000804364040@756a17a6 // ) The acquired GTFS resource gives access to the content under the read namespace. The content is streamed entity by entity. This way the files are never entirely loaded into memory when reading them. The read namespace exposes function to read from the standard files, for instance if one wants to read the available route names from a GTFS file, on can use the routes function as follows. Note that it uses the provided data model. gtfs.use { gtfs =&gt; gtfs.read .routes[ExtendedRoute].collect { case Route(id, _, Some(name), _, _, _, _, _, _, _) =&gt; s\"$name ($id)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 (17514_400) // M5 (17459_900) // 123 (17304_700) The read namespace contains shortcuts to read entities from the standard files. You need to provide the type you want to decode the entities to (in this example ExtendedRoute, which is the route entity using extended route types). You can provide your own type, provided that you also provide a CsvRowDecoder for that type. For instance if you are only interested in extracting route name and identifier, you can define you own data model for these two fields. import fs2.data.csv.CsvRowDecoder import fs2.data.csv.generic.CsvName import fs2.data.csv.generic.semiauto._ case class IdNameRoute( @CsvName(\"route_id\") id: String, @CsvName(\"route_short_name\") name: Option[String]) object IdNameRoute { implicit val decoder: CsvRowDecoder[IdNameRoute, String] = deriveCsvRowDecoder } gtfs.use { gtfs =&gt; gtfs.read .routes[IdNameRoute].collect { case IdNameRoute(id, Some(name)) =&gt; s\"$name ($id)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 (17514_400) // M5 (17459_900) // 123 (17304_700) The simplest way to get the proper decoder for your own case classes is to use the fs2-data generic module as shown in the example above. Non standard files If you want to access files that are not part of the GTFS standard, you can use the file function, which takes the file name. Note: The file has to be a valid CSV file. For instance, to access a contributors.txt file that would list the contributors of the file, you can use this function. case class Contributor(name: String, email: String) object Contributor { implicit val decoder: CsvRowDecoder[Contributor, String] = deriveCsvRowDecoder } gtfs.use { gtfs =&gt; gtfs.read .file[Contributor](\"contributors.txt\").map { case Contributor(name, email) =&gt; s\"$name ($email)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // VBB (info@vbb.de) // Mobimeo (opensource@mobimeo.com) Raw rows For some usage, you might not want to deserialize the rows to a typed data model, but want to work with raw CSV rows from the files. This is useful for instance in case you want to modify the values of a field without validating or needing to know what the other fields contain. The GtfsFile class provides a raw variant for every file access. For instance, if you want to extract the route names without deserializing, you can use this approach. gtfs.use { gtfs =&gt; gtfs.read .rawRoutes .map(s =&gt; s(\"route_short_name\")) .unNone .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 // M5 // 123"
    } ,    
    {
      "title": "Writing to a GTFS file",
      "url": "/fs2-gtfs/documentation/file/writing/",
      "content": "Writing to a GTFS file Modifying an existing file Creating a new file Writing to a GTFS file Similarly reading GTFS file, one can write GTFS files easily using this library. You can either modify an existing file, or create a new one from scratch. The writing pipes, live in the write namespace within the GtfsFile class. This namespace provides a handful of Pipes which give access to standard GTFS files. import com.mobimeo.gtfs.file._ import com.mobimeo.gtfs.model._ import cats.effect._ import cats.effect.unsafe.implicits.global import fs2.io.file.{CopyFlag, CopyFlags, Path} val gtfs = GtfsFile[IO](Path(\"site/gtfs.zip\")) // gtfs: Resource[IO, GtfsFile[IO]] = Bind( // source = Allocate( // resource = cats.effect.kernel.Resource$$$Lambda$16478/0x0000000804362840@1554ed9 // ), // fs = cats.effect.kernel.Resource$$Lambda$16480/0x0000000804364040@35cd7257 // ) Modifying an existing file Oftentimes you already have an existing file in your hands and you want to modify it. To this end, you can pipe the read stream into the corresponding pipe. gtfs.use { gtfs =&gt; gtfs.read .rawStops .map(s =&gt; s.modify(\"stop_name\")(_.toUpperCase)) .through(gtfs.write.rawStops) .compile .drain } This code modifies the file in place, making all stop names uppercase. However this is usually not recommended as data are overwritten and original data are replaced. One should prefer to work on a copy of the original file. The GtfsFile provides a way to do it conveniently. val modified = Path(\"site/modified-gtfs.zip\") // modified: Path = site/modified-gtfs.zip gtfs.use { src =&gt; src.copyTo(modified, CopyFlags(CopyFlag.ReplaceExisting)).use { tgt =&gt; src.read .rawStops .map(s =&gt; s.modify(\"stop_name\")(_.toUpperCase)) .through(tgt.write.rawStops) .compile .drain } }.unsafeRunSync() def printStops(gtfs: GtfsFile[IO]) = gtfs.read .rawStops .map(s =&gt; s(\"stop_name\")) .unNone .take(5) .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain // original file gtfs.use(printStops(_)).unsafeRunSync() // S+U Berlin Hauptbahnhof // S+U Berlin Hauptbahnhof // Berlin, Friedrich-Olbricht-Damm/Saatwinkler Damm // Berlin, Stieffring // Berlin, Lehrter Str./Invalidenstr. // modified file GtfsFile[IO](modified).use(printStops(_)).unsafeRunSync() // S+U BERLIN HAUPTBAHNHOF // S+U BERLIN HAUPTBAHNHOF // BERLIN, FRIEDRICH-OLBRICHT-DAMM/SAATWINKLER DAMM // BERLIN, STIEFFRING // BERLIN, LEHRTER STR./INVALIDENSTR. When using copyTo the entirety of the original GTFS file content is copied and only files that are written to are modified. The rest is identical to the original file (including potential non standard files). Creating a new file If one wants to create a new file from scratch, one need to tell the file needs to be created when creating the GTFS resource. An empty GTFS file will be created, and files can be added to it by using the associated write pipes. def makeStop(id: String, name: String) = Stop(id, None, Some(name), None, None, None, None, None, None, None, None, None, None, None) val file = Path(\"site/gtfs2.zip\") // file: Path = site/gtfs2.zip GtfsFile[IO](file, create = true).use { gtfs =&gt; fs2.Stream.emits(List(makeStop(\"stop1\", \"Some Stop\"), makeStop(\"stop2\", \"Some Other Stop\"))) .covary[IO] .through(gtfs.write.stops[Stop]) .compile .drain }.unsafeRunSync() GtfsFile[IO](file).use(printStops(_)).unsafeRunSync() // Some Stop // Some Other Stop"
    } ,    
    {
      "title": "Working with GTFS files",
      "url": "/fs2-gtfs/documentation/file/",
      "content": "Working with GTFS files Implementation details Working with GTFS files The core module provides a way to work with GTFS files as described by the standard. The file is accessed wrapped within a Resource to ensure it is released properly when done working with it. This API lives in the com.mobimeo.gtfs.file package import com.mobimeo.gtfs.file._ import fs2.io.file.Path import cats.effect._ import cats.effect.unsafe.implicits.global GtfsFile[IO](Path(\"site/gtfs.zip\")).use { gtfs =&gt; IO.pure(s\"Some work with the GTFS file at ${gtfs.file}\") }.unsafeRunSync() // res0: String = \"Some work with the GTFS file at site/gtfs.zip\" Within the use scope you can use the gtfs reference to read from and write to the file. To achieve this, have a look at the dedicated pages: Reading from a GTFS file Writing to a GTFS file Implementation details The GtfsFile class is implemented in a way that doesn’t require to load the entire GTFS file into memory. The zip file is mapped to a FileSystem and files are accessed through this API, under the hood. Reading a CSV file from the GTFS data, streams the content, by default only loading in memory what is necessary to process the current CSV row. The class is also implemented in a way that makes it possible to modify files in place, even though it is recommended to work on a copy."
    } ,    
    {
      "title": "Data Model",
      "url": "/fs2-gtfs/documentation/model/",
      "content": "The GTFS data model The core library provides case classes encoding the entity as described in the GTFS standard. These classes can be used to read and write GTFS data when you are dealing with files respecting the standard. The entities are typed and can be used safely for transformations. GTFS Routes Routes have a field indicating their type. The GTFS standard defines only 13 of them, but there is an extension for more fine-grained route types. The default model provided by the library allows for parameterizing routes with the route type type. One can use Int to get the raw type, but it also defines enumerations for types. Depending on the type used by the GTFS files, you can read routes using: // use this if your GTFS file uses the simple GTFS route types. This is an alias for `Route[SimpleRouteType]` gtfs.read.routes[SimpleRoute] // use this if your GTFS file uses the extended GTFS route types. This is an alias for `Route[ExtendedRouteType]` gtfs.read.routes[ExtendedRoute] // use this if your GTFS file uses both the simple and the extended GTFS route types. This is an alias for `Route[Either[SimpleRouteType, ExtendedRouteType]]` gtfs.read.routes[EitherRoute]"
    } ,    
    {
      "title": "External DSL",
      "url": "/fs2-gtfs/documentation/rules/dsl/external/",
      "content": "The external DSL The DSL syntax Parsing the DSL Using the RuleParser class directly Error reporting Using the interpolators Error reporting Pretty printing The external DSL To use the GTFS rule syntax, add the dependency to your build file. For instance for sbt: libraryDependencies += \"com.mobimeo\" %% \"fs2-gtfs-rules-syntax\" % \"&lt;version&gt;\" It is cross compiled for scala 2.13 and scala 3. The external DSL allows you to write rules in a declarative way without any knowledge of scala. This can be useful if you intend to have non developer to write the rules, or if you want to store them in a text file or database, without needing to compile scala code when they are changed. The DSL syntax The DSL syntax follows the following grammar. file = ruleset* ruleset = 'ruleset' 'for' filename '{' rule ('or' rule)* '}' filename = 'agency' | 'stops' | 'routes' | 'trips' | 'stop_times' | 'calendar' | 'calendar_dates' | 'fare_attributes' | 'fare_rules' | 'shapes' | 'frequencies' | 'transfers' | 'pathways' | 'levels' | 'feed_info' | 'translations' | 'attributions' rule = 'rule' ident '{' 'when' matcher 'then' action '}' matcher = '(' matcher ')' | '!' matcher | 'any' | variable 'exists' | value valueMatcher | matcher 'and' matcher | matcher 'or' matcher valueMatcher = '==' value | 'in' '[' value* ']' | 'matches' regex action = 'delete' | transformation ('then' transformation)* | log transformation = 'set' 'row' '[' str ']' 'to' value | 'search' regex 'in' 'row' '[' str ']' 'and' 'replace' 'by' value log = 'debug' '(' expr ')' | 'info' '(' expr ')' | 'warning' '(' expr ')' | 'error' '(' expr ')' expr = ident '(' (expr (',' expr)*)? ')' | value | expr + expr value = str | variable variable = 'row' '[' value ']' | 'ctx' '[' value ']' ('[' value ']')* ident = [a-zA-Z] [a-zA-Z0-9_-]* str = '\"' &lt;any character with \" and \\ escaped&gt; '\"' regex = '/' &lt;any character with / and \\ escaped&gt; '/' Matcher operators have the following precedence: ! has precedence over and which as precedence over or. This means that !m1 and m2 or m3 is equivalent to ((!m1) and m2) or m3. Combinators are left associative, i.e. m1 and m2 and m3 is equivalent to (m1 and m2) and m3. Parsing the DSL There are two way to parse rules written with the external DSL: Using the RuleParser combinators from the com.mobimeo.gtfs.rules.syntax package. Using the string interpolators provided in the com.mobimeo.gtfs.rules.syntax.lexicals package. Note: Rules parsed with the external DSL are effect free, only rules with Id are generated. The effectful rules only come from the ones using anonymous scala functions, which cannot be expressed with the DSL. Using the RuleParser class directly The parser is based on cats-parse and exposes different entry points to parse a file which consists of zero or more rule sets, a ruleset which defines a single set of rules, a rule which defines a single rule. For instance, you can express the rule that makes stop names uppercase as follows: import com.mobimeo.gtfs.rules.syntax._ RuleParser.ruleset.parseAll(\"\"\"ruleset for stops { | rule uppercase-stops { | when row[\"stop_name\"] exists | then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) | } |}\"\"\".stripMargin) // res0: Either[cats.parse.Parser.Error, com.mobimeo.gtfs.rules.RuleSet[cats.package.Id]] = Right( // value = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"uppercase-stops\", // matcher = Exists(variable = Field(name = Str(value = \"stop_name\"))), // action = Transform( // transformations = NonEmptyList( // head = Set( // field = Str(value = \"stop_name\"), // to = NamedFunction( // name = \"uppercase\", // args = List(Val(v = Field(name = Str(value = \"stop_name\")))) // ) // ), // tail = List() // ) // ) // ) // ), // additions = List() // ) // ) Error reporting cats-parse does not (yet) provide a nice pretty printing for parse errors, but we provide a module that can be used to render them nicely, namely the Prettyprint class. import cats.syntax.all._ val input = \"\"\"rule wrong-uppercase-stops { | when row[\"stop_name\"] exist | then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) |}\"\"\".stripMargin // input: String = \"\"\"rule wrong-uppercase-stops { // when row[\"stop_name\"] exist // then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) // }\"\"\" RuleParser.rule.parseAll(input).leftMap(error =&gt; new Prettyprint(input).prettyprint(error)) // res1: Either[String, com.mobimeo.gtfs.rules.Rule[cats.package.Id]] = Left( // value = \"\"\" // 2:25: error: expected one of '==', 'exists', 'in', 'matches' // 2 | when row[\"stop_name\"] exist // | ^\"\"\" // ) Using the interpolators When the rules you want to parse are known at compile time and you would like to be notified of syntax errors at compile time, you can use the provided interpolators from package com.mobimeo.gtfs.rules.syntax.literals. The rules defined with these literals are parsed and checked at compile time, and they return the rules themselves, no wrapped in any Either. import com.mobimeo.gtfs.rules.syntax.literals._ val rules = ruleset\"\"\"ruleset for stops { rule uppercase-stops { when row[\"stop_name\"] exists then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) } }\"\"\" // rules: com.mobimeo.gtfs.rules.RuleSet[cats.package.Id] = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"uppercase-stops\", // matcher = Exists(variable = Field(name = Str(value = \"stop_name\"))), // action = Transform( // transformations = NonEmptyList( // head = Set( // field = Str(value = \"stop_name\"), // to = NamedFunction( // name = \"uppercase\", // args = List(Val(v = Field(name = Str(value = \"stop_name\")))) // ) // ), // tail = List() // ) // ) // ) // ), // additions = List() // ) Error reporting Since rules are parsed at compile time, error reporting occurs when compiling the scala code in the scala compiler. rule\"\"\"rule wrong-uppercase-stops { when row[\"stop_name\"] exist then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) }\"\"\" // error: // 2:32: error: expected one of '==', 'exists', 'in', 'matches' // 2 | when row[\"stop_name\"] exist // | ^ // rule\"\"\"rule wrong-uppercase-stops { // ^ The code snippet above does not compile at all. Pretty printing If you have set of rules that you’d like to serialize to the concrete syntax, you can use the Show instances available in the com.mobimeo.gtfs.rules.syntax.pretty package. import com.mobimeo.gtfs.rules.syntax.pretty._ rules.show // res3: String = \"\"\"ruleset for stops { // rule uppercase-stops { // when row[\"stop_name\"] exists // then set row[\"stop_name\"] to uppercase(row[\"stop_name\"]) // } // }\"\"\" Note: the concrete syntax does not allow for expressing anonymous functions. If you try to serialize rules containing calls to anonymous functions, it will be rendered as concatenation of the arguments. import cats.Id import com.mobimeo.gtfs.rules._ val fun = (s: List[String]) =&gt; s.mkString(\"-\") // fun: List[String] =&gt; String = &lt;function1&gt; val t: Expr[Id] = Expr.AnonymousFunction[Id](fun, List( Expr.Val(Value.Str(\"a\")), Expr.Val(Value.Str(\"b\")), Expr.Val(Value.Str(\"c\")))) // t: Expr[Id] = AnonymousFunction( // f = &lt;function1&gt;, // args = List( // Val(v = Str(value = \"a\")), // Val(v = Str(value = \"b\")), // Val(v = Str(value = \"c\")) // ) // ) t.show // res4: String = \"\\\"a\\\" + \\\"b\\\" + \\\"c\\\"\""
    } ,    
    {
      "title": "Embedded DSL",
      "url": "/fs2-gtfs/documentation/rules/dsl/embedded/",
      "content": "The embedded DSL Define a rule set Access to the current row and context The row matcher Perform transformations Delete a row Log data The embedded DSL The embedded DSL allows you to write scala code to type-safely create rule sets in a convenient way. Rules are grouped in rule sets, applying to a specific file in GTFS data. The DSL helps you write your rules in scala in an easy and typesafe way. Define a rule set To define a set of rule, you need to have an instance of the Dsl class for you effect type. By importing members of this class, you bring in scope the facilities. import com.mobimeo.gtfs._ import com.mobimeo.gtfs.rules._ import cats.effect._ val dsl = new Dsl[IO] // dsl: Dsl[IO] = com.mobimeo.gtfs.rules.Dsl@454570 import dsl._ Rule sets are defined within the ruleset method. The first parameter defines which file the set applies to within the GTFS data, the second part defines the rules included in the rule set. The different rules will be tried in order, and the first matching one will be selected for each row in the file. To write several rules, you can separate them with the orElse operator. val someRule: RulesBuilder = ??? val someOtherRule: RulesBuilder = ??? ruleset(StandardName.Routes) { someRule orElse someOtherRule } Access to the current row and context Within the definition of a rule (matcher and transformations) it might come in handy to access the content of the current row. This can be achieved by using the row variable available in the DSL. For instance to access the route_short_name field of the current row (if it exists): row(\"route_short_name\") // res1: RowFieldBuilder = com.mobimeo.gtfs.rules.Dsl$RowFieldBuilder@100cb7ba Sometimes, not only the current row value is interesting but we might want to use some sort of global context, containing values. The DSL provides the ctx variable, which gives you access to the hierarchical context, which consists of a tree of string. Accessing values can be done by providing the path to the leaf. For instance, let’s say you have the current date in the global context as follows: { \"meta\": { \"date\": \"2021-01-01\" } } You can access the date field like this: ctx(\"meta\")(\"date\") // res2: CtxBuilder = com.mobimeo.gtfs.rules.Dsl$CtxBuilder@269bcd31 Both row and ctx can be used in the matcher part of the rule and the transformation one. The row matcher The first part of a rule is the matcher. The matcher defines which rows are eligible for performing the actions of the rule. Note: Matcher is a pure expression, it cannot perform any side effect, that is why it is not possible to call functions in a matcher. The matcher expressions are usually based on the content of the current row. To check taht a row contains a field, you can use the exists operator: row(\"stop_name\").exists // res3: Matcher = Exists(variable = Field(name = Str(value = \"stop_name\"))) Values can be compared using the === operator: row(\"stop_name\") === \"Some Stop\" // res4: Matcher = Equals( // left = Field(name = Str(value = \"stop_name\")), // right = Str(value = \"Some Stop\") // ) You can also check that a value is withing a list of values using the in operator: row(\"stop_name\") in List(\"Some Stop\", \"Some Other Stop\") // res5: Matcher = In( // value = Field(name = Str(value = \"stop_name\")), // values = List(Str(value = \"Some Stop\"), Str(value = \"Some Other Stop\")) // ) You can check for some regular expression patterns using the matches operator: row(\"stop_name\") matches \"^Berlin, .*\" // res6: Matcher = Matches( // value = Field(name = Str(value = \"stop_name\")), // regex = \"^Berlin, .*\" // ) Matchers can be combined together via and, or, and ! operators to create a new one: row(\"stop_name\").exists and !row(\"stop_id\").exists // res7: Matcher = And( // left = Exists(variable = Field(name = Str(value = \"stop_name\"))), // right = Not(inner = Exists(variable = Field(name = Str(value = \"stop_id\")))) // ) Perform transformations One possible kind of rules are the rules that perform transformations on the matched rows. We can for instance define the rules to make all stop names uppercase as follows: ruleset(StandardName.Stops) { rule(\"uppercase-stops\") .when(any) .perform(row(\"stop_name\") := uppercase(row(\"stop_name\"))) } // res8: RuleSet[IO] = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"uppercase-stops\", // matcher = Any, // action = Transform( // transformations = NonEmptyList( // head = Set( // field = Str(value = \"stop_name\"), // to = NamedFunction( // name = \"uppercase\", // args = List(Val(v = Field(name = Str(value = \"stop_name\")))) // ) // ), // tail = List() // ) // ) // ) // ), // additions = List() // ) The previous transformation sets the row field value to a new value. Another possible transformation is to search for a pattern and replace it by a new value. For instance, if we want to remove the leading Berlin, occurrences in stop names: ruleset(StandardName.Stops) { rule(\"uppercase-stops\") .when(row(\"stop_name\") matches \"^Berlin, .*$\") .perform(in(\"stop_name\").search(\"^Berlin, \").andReplaceBy(\"\")) } // res9: RuleSet[IO] = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"uppercase-stops\", // matcher = Matches( // value = Field(name = Str(value = \"stop_name\")), // regex = \"^Berlin, .*$\" // ), // action = Transform( // transformations = NonEmptyList( // head = SearchAndReplace( // field = Str(value = \"stop_name\"), // regex = \"^Berlin, \", // replacement = \"\" // ), // tail = List() // ) // ) // ) // ), // additions = List() // ) In the replacement string, $n refers to the value of the n-th capturing group in the regular expression for n between 0 and 9. The perform function takes several transformations (at least one) which will be applied in order of defnition. A transformation is performed on the result of the previous ones. Delete a row The second action a rule can perform is deleting matching rows. For instance if a stop was closed but still appears in GTFS data, you could add this rule: ruleset(StandardName.Stops) { rule(\"delete-französische-straße\") .when(row(\"route_short_name\") === \"Französische Straße\") .delete } // res10: RuleSet[IO] = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"delete-franz\\u00f6sische-stra\\u00dfe\", // matcher = Equals( // left = Field(name = Str(value = \"route_short_name\")), // right = Str(value = \"Franz\\u00f6sische Stra\\u00dfe\") // ), // action = Delete() // ) // ), // additions = List() // ) Log data The last possible action to perform for a rule is logging. The logging action will be executed for the matching rows only. Rows (matching or not) are always left unchanged for logging rules. For instance a rule set that logs missing colors for routes could look like this: ruleset(StandardName.Routes) { rule(\"log-missing-colors\") .when(!row(\"route_color\").exists and !row(\"route_text_color\").exists) .error(concat\"Route ${row(\"route_short_name\")} (${row(\"route_id\")}) is missing all colors\") orElse rule(\"log-missing-line-color\") .when(!row(\"route_color\").exists) .error(concat\"Route ${row(\"route_short_name\")} (${row(\"route_id\")}) is missing line color\") orElse rule(\"log-missing-text-color\") .when(!row(\"route_color\").exists) .warning(concat\"Route ${row(\"route_short_name\")} (${row(\"route_id\")}) is missing text color\") } // res11: RuleSet[IO] = RuleSet( // file = \"routes.txt\", // rules = List( // Rule( // name = \"log-missing-colors\", // matcher = Not( // inner = Or( // left = Exists(variable = Field(name = Str(value = \"route_color\"))), // right = Exists( // variable = Field(name = Str(value = \"route_text_color\")) // ) // ) // ), // action = Log( // msg = NamedFunction( // name = \"concat\", // args = List( // Val(v = Str(value = \"Route \")), // Val(v = Field(name = Str(value = \"route_short_name\"))), // Val(v = Str(value = \" (\")), // Val(v = Field(name = Str(value = \"route_id\"))), // Val(v = Str(value = \") is missing all colors\")) // ) // ), // level = Error // ) // ), // Rule( // name = \"log-missing-line-color\", // matcher = Not( // inner = Exists(variable = Field(name = Str(value = \"route_color\"))) // ), // action = Log( // msg = NamedFunction( // name = \"concat\", // args = List( // Val(v = Str(value = \"Route \")), // Val(v = Field(name = Str(value = \"route_short_name\"))), // Val(v = Str(value = \" (\")), // Val(v = Field(name = Str(value = \"route_id\"))), // Val(v = Str(value = \") is missing line color\")) // ) // ), // level = Error // ) // ), // Rule( // name = \"log-missing-text-color\", // matcher = Not( // inner = Exists(variable = Field(name = Str(value = \"route_color\"))) // ), // action = Log( // msg = NamedFunction( // name = \"concat\", // args = List( // Val(v = Str(value = \"Route \")), // Val(v = Field(name = Str(value = \"route_short_name\"))), // Val(v = Str(value = \" (\")), // Val(v = Field(name = Str(value = \"route_id\"))), // Val(v = Str(value = \") is missing text color\")) // ) // ), // level = Warning // ) // ) // ), // additions = List() // )"
    } ,    
    {
      "title": "GTFS Rule Engine",
      "url": "/fs2-gtfs/documentation/rules/",
      "content": "GTFS rule engine The rules Create the engine Execute the rules Default functions GTFS rule engine To use the GTFS rule engine, add the dependency to your build file. For instance for sbt: libraryDependencies += \"com.mobimeo\" %% \"fs2-gtfs-rules\" % \"&lt;version&gt;\" It is cross compiled for scala 2.13 and scala 3. The GTFS rule engine provides a declarative way of describing checks and transformations of GTFS data. This is useful if you need to verify and normalize your GTFS data before using them (e.g. in a pre-processing pipeline). The idea of this module is to provide a clear DSL to handle the data in a declarative way. To understand the rationale behind this module, you can read the blog post series we published. The rules The rules are grouped in sets. A rule set applies to a given file in the GTFS data (e.g. routes.txt) and defines a list of rules. For each row in the file, the rules are tried in order. The first matching one is taken an its associated action is executed. If no rule matches for the current row, then the row is left unchanged. Note: the semantics for rules in a rule set is similar to the on of cases in a pattern match. The order matters as only the first matching one gets selected. Rules are composed of two parts: A matcher, which defines which rows this rule applies to. An action, which defines the action to perform when a row is selected by the matcher. We can for instance define a rule set that makes the station name uppercase: import com.mobimeo.gtfs._ import com.mobimeo.gtfs.rules._ import cats.effect._ import cats.data.NonEmptyList import cats.syntax.all._ val rules = RuleSet( StandardName.Stops.entryName, List( Rule( \"uppercase-stops\", Matcher.Any, Action.Transform( NonEmptyList.one( Transformation.Set[IO]( Value.Str(\"stop_name\"), Expr.NamedFunction( \"uppercase\", List(Expr.Val(Value.Field(Value.Str(\"stop_name\")))))))))), Nil) // rules: RuleSet[IO[A]] = RuleSet( // file = \"stops.txt\", // rules = List( // Rule( // name = \"uppercase-stops\", // matcher = Any, // action = Transform( // transformations = NonEmptyList( // head = Set( // field = Str(value = \"stop_name\"), // to = NamedFunction( // name = \"uppercase\", // args = List(Val(v = Field(name = Str(value = \"stop_name\")))) // ) // ), // tail = List() // ) // ) // ) // ), // additions = List() // ) As you can see, this is not the easiest way to define the rules, that’s why the library also provides an embedded DSL and an external DSL to help write them in a more readable way. Create the engine The base class to know to run rules on your data is the Engine that lives in the com.mobimeo.gtfs.rules package. import org.typelevel.log4cats.slf4j.Slf4jLogger // this is unsafe in production code, please refer to the log4cats documentation implicit val unsafeLogger = Slf4jLogger.getLogger[IO] // unsafeLogger: org.typelevel.log4cats.SelfAwareStructuredLogger[IO] = org.typelevel.log4cats.slf4j.internal.Slf4jLoggerInternal$Slf4jLogger@74f0cdc0 val engine = Engine[IO] // engine: Engine[IO] = com.mobimeo.gtfs.rules.Engine@6f6b66dd An engine can be reused with different sets of rules and GTFS files. Execute the rules Once you have an engine and rules, you can apply them to GTFS data using the process function. import cats.effect.unsafe.implicits.global import com.mobimeo.gtfs.file.GtfsFile import fs2.io.file._ val gtfs = GtfsFile[IO](Path(\"site/gtfs.zip\")) // gtfs: Resource[IO, GtfsFile[IO]] = Bind( // source = Allocate( // resource = cats.effect.kernel.Resource$$$Lambda$16478/0x0000000804362840@6b9fbec4 // ), // fs = cats.effect.kernel.Resource$$Lambda$16480/0x0000000804364040@1973f0ba // ) val modified = Path(\"site/modified-rules-gtfs.zip\") // modified: Path = site/modified-rules-gtfs.zip gtfs.use { src =&gt; src.copyTo(modified, CopyFlags(CopyFlag.ReplaceExisting)).use { tgt =&gt; engine.process(List(rules), src, tgt) } }.unsafeRunSync() def printStops(gtfs: GtfsFile[IO]) = gtfs.read .rawStops .map(s =&gt; s(\"stop_name\")) .unNone .take(5) .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain // original file gtfs.use(printStops(_)).unsafeRunSync() // S+U Berlin Hauptbahnhof // S+U Berlin Hauptbahnhof // Berlin, Friedrich-Olbricht-Damm/Saatwinkler Damm // Berlin, Stieffring // Berlin, Lehrter Str./Invalidenstr. // modified file GtfsFile[IO](modified).use(printStops(_)).unsafeRunSync() // S+U BERLIN HAUPTBAHNHOF // S+U BERLIN HAUPTBAHNHOF // BERLIN, FRIEDRICH-OLBRICHT-DAMM/SAATWINKLER DAMM // BERLIN, STIEFFRING // BERLIN, LEHRTER STR./INVALIDENSTR. All rule sets provided to the process function are run in order. You can have several rule set applying to the same file, all of them will be applied to the file in the order they are defined. Default functions The library provides a set of standard functions you can use from the rules. These are available in Interpreter.defaultFunctions. The function available by default are: lowercase: (str) -&gt; str Makes the argument lowercase uppercase: (str) -&gt; str Makes the argument uppercase trim: (str) -&gt; str Trims the argument concat: (str*) -&gt; str Concatenates all arguments"
    } ,    
    {
      "title": "Documentation",
      "url": "/fs2-gtfs/documentation/",
      "content": "Welcome to the fs2-gtfs documentation page. You will find here all the information about the library and its modules. Please refer to the menu on the left side. Core Module To use the core module, add the dependency to your build file. For instance for sbt: libraryDependencies += \"com.mobimeo\" %% \"fs2-gtfs-core\" % \"&lt;version&gt;\" It is cross compiled for scala 2.13 and scala 3. The core of this library is the Gtfs interface, that provides the generic API for wroking with GTFS files. The API is based on namespaces, grouping together the operators by responsibility. The Gtfs interface provides the following namespaces: read provides streaming read access to the content of the GTFS data write provides streaming write access to the content of the GTFS data has provides ways of checking whether a file exists in the GTFS data delete provides functions to delete a file from the GTFS data This interface provides the basis features for working with GTFS data, independently from where and how it is stored. Concrete implementations will provide this interface and might extend it with implementation specific functions. Have a look at the documented implementation to choose the one that fits your needs."
    } ,          
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
