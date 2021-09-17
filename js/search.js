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
      "content": "Reading from a GTFS file Non standard files Raw rows Reading from a GTFS file The GTFS standard defines the format in which a GTFS file is shared. It consists in a bunch of CSV files within a zip file. import com.mobimeo.gtfs.file._ import com.mobimeo.gtfs.model._ import cats.effect._ import cats.effect.unsafe.implicits.global import java.nio.file._ val gtfs = GtfsFile[IO](Paths.get(\"site/gtfs.zip\")) // gtfs: Resource[IO, GtfsFile[IO]] = Bind( // source = Allocate( // resource = cats.effect.kernel.Resource$$$Lambda$15717/0x00000008028af040@31fcc4da // ), // fs = cats.effect.kernel.Resource$$Lambda$15719/0x00000008028ad840@4c8f0b5b // ) The acquired GTFS resource gives access to the content under the read namespace. The content is streamed entity by entity. This way the files are never entirely loaded into memory when reading them. The read namespace exposes function to read from the standard files, for instance if one wants to read the available route names from a GTFS file, on can use the routes function as follows. Note that it uses the provided data model. gtfs.use { gtfs =&gt; gtfs.read .routes[ExtendedRoute].collect { case Route(id, _, Some(name), _, _, _, _, _, _, _) =&gt; s\"$name ($id)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 (17514_400) // M5 (17459_900) // 123 (17304_700) The read namespace contains shortcuts to read entities from the standard files. You need to provide the type you want to decode the entities to (in this example ExtendedRoute, which is the route entity using extended route types). You can provide your own type, provided that you also provide a CsvRowDecoder for that type. For instance if you are only interested in extracting route name and identifier, you can define you own data model for these two fields. import fs2.data.csv.CsvRowDecoder import fs2.data.csv.generic.CsvName import fs2.data.csv.generic.semiauto._ case class IdNameRoute( @CsvName(\"route_id\") id: String, @CsvName(\"route_short_name\") name: Option[String]) object IdNameRoute { implicit val decoder: CsvRowDecoder[IdNameRoute, String] = deriveCsvRowDecoder } gtfs.use { gtfs =&gt; gtfs.read .routes[IdNameRoute].collect { case IdNameRoute(id, Some(name)) =&gt; s\"$name ($id)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 (17514_400) // M5 (17459_900) // 123 (17304_700) The simplest way to get the proper decoder for your own case classes is to use the fs2-data generic module as shown in the example above. Non standard files If you want to access files that are not part of the GTFS standard, you can use the file function, which takes the file name. Note: The file has to be a valid CSV file. For instance, to access a contributors.txt file that would list the contributors of the file, you can use this function. case class Contributor(name: String, email: String) object Contributor { implicit val decoder: CsvRowDecoder[Contributor, String] = deriveCsvRowDecoder } gtfs.use { gtfs =&gt; gtfs.read .file[Contributor](\"contributors.txt\").map { case Contributor(name, email) =&gt; s\"$name ($email)\" } .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // VBB (info@vbb.de) // Mobimeo (opensource@mobimeo.com) Raw rows For some usage, you might not want to deserialize the rows to a typed data model, but want to work with raw CSV rows from the files. This is useful for instance in case you want to modify the values of a field without validating or needing to know what the other fields contain. The GtfsFile class provides a raw variant for every file access. For instance, if you want to extract the route names without deserializing, you can use this approach. gtfs.use { gtfs =&gt; gtfs.read .rawRoutes .map(s =&gt; s(\"route_short_name\")) .unNone .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain }.unsafeRunSync() // U2 // M5 // 123"
    } ,    
    {
      "title": "Writing to a GTFS file",
      "url": "/fs2-gtfs/documentation/file/writing/",
      "content": "Writing to a GTFS file Modifying an existing file Creating a new file Writing to a GTFS file Similarly reading GTFS file, one can write GTFS files easily using this library. You can either modify an existing file, or create a new one from scratch. The writing pipes, live in the write namespace within the GtfsFile class. This namespace provides a handful of Pipes which give access to standard GTFS files. import com.mobimeo.gtfs.file._ import com.mobimeo.gtfs.model._ import cats.effect._ import cats.effect.unsafe.implicits.global import java.nio.file._ val gtfs = GtfsFile[IO](Paths.get(\"site/gtfs.zip\")) // gtfs: Resource[IO, GtfsFile[IO]] = Bind( // source = Allocate( // resource = cats.effect.kernel.Resource$$$Lambda$15717/0x00000008028af040@71ba07b4 // ), // fs = cats.effect.kernel.Resource$$Lambda$15719/0x00000008028ad840@52236b41 // ) Modifying an existing file Oftentimes you already have an existing file in your hands and you want to modify it. To this end, you can pipe the read stream into the corresponding pipe. gtfs.use { gtfs =&gt; gtfs.read .rawStops .map(s =&gt; s.modify(\"stop_name\")(_.toUpperCase)) .through(gtfs.write.rawStops) .compile .drain } This code modifies the file in place, making all stop names uppercase. However this is usually not recommended as data are overwritten and original data are replaced. One should prefer to work on a copy of the original file. The GtfsFile provides a way to do it conveniently. val modified = Paths.get(\"site/modified-gtfs.zip\") // modified: Path = site/modified-gtfs.zip gtfs.use { src =&gt; src.copyTo(modified, List(StandardCopyOption.REPLACE_EXISTING)).use { tgt =&gt; src.read .rawStops .map(s =&gt; s.modify(\"stop_name\")(_.toUpperCase)) .through(tgt.write.rawStops) .compile .drain } }.unsafeRunSync() def printStops(gtfs: GtfsFile[IO]) = gtfs.read .rawStops .map(s =&gt; s(\"stop_name\")) .unNone .take(5) .intersperse(\"\\n\") .evalMap(s =&gt; IO(print(s))) .compile .drain // original file gtfs.use(printStops(_)).unsafeRunSync() // S+U Berlin Hauptbahnhof // S+U Berlin Hauptbahnhof // Berlin, Friedrich-Olbricht-Damm/Saatwinkler Damm // Berlin, Stieffring // Berlin, Lehrter Str./Invalidenstr. // modified file GtfsFile[IO](modified).use(printStops(_)).unsafeRunSync() // S+U BERLIN HAUPTBAHNHOF // S+U BERLIN HAUPTBAHNHOF // BERLIN, FRIEDRICH-OLBRICHT-DAMM/SAATWINKLER DAMM // BERLIN, STIEFFRING // BERLIN, LEHRTER STR./INVALIDENSTR. When using copyTo the entirety of the original GTFS file content is copied and only files that are written to are modified. The rest is identical to the original file (including potential non standard files). Creating a new file If one wants to create a new file from scratch, one need to tell the file needs to be created when creating the GTFS resource. An empty GTFS file will be created, and files can be added to it by using the associated write pipes. def makeStop(id: String, name: String) = Stop(id, None, Some(name), None, None, None, None, None, None, None, None, None, None, None) val file = Paths.get(\"site/gtfs2.zip\") // file: Path = site/gtfs2.zip GtfsFile[IO](file, create = true).use { gtfs =&gt; fs2.Stream.emits(List(makeStop(\"stop1\", \"Some Stop\"), makeStop(\"stop2\", \"Some Other Stop\"))) .covary[IO] .through(gtfs.write.stops[Stop]) .compile .drain }.unsafeRunSync() GtfsFile[IO](file).use(printStops(_)).unsafeRunSync() // Some Stop // Some Other Stop"
    } ,    
    {
      "title": "Working with GTFS files",
      "url": "/fs2-gtfs/documentation/file/",
      "content": "Working with GTFS files Implementation details Working with GTFS files The core module provides a way to work with GTFS files as described by the standard. The file is accessed wrapped within a Resource to ensure it is released properly when done working with it. This API lives in the com.mobimeo.gtfs.file package import com.mobimeo.gtfs.file._ import cats.effect._ import cats.effect.unsafe.implicits.global import java.nio.file._ GtfsFile[IO](Paths.get(\"site/gtfs.zip\")).use { gtfs =&gt; IO.pure(s\"Some work with the GTFS file at ${gtfs.file}\") }.unsafeRunSync() // res0: String = \"Some work with the GTFS file at site/gtfs.zip\" Within the use scope you can use the gtfs reference to read from and write to the file. To achieve this, have a look at the dedicated pages: Reading from a GTFS file Writing to a GTFS file Implementation details The GtfsFile class is implemented in a way that doesn’t require to load the entire GTFS file into memory. The zip file is mapped to a FileSystem and files are accessed through this API, under the hood. Reading a CSV file from the GTFS data, streams the content, by default only loading in memory what is necessary to process the current CSV row. The class is also implemented in a way that makes it possible to modify files in place, even though it is recommended to work on a copy."
    } ,    
    {
      "title": "Data Model",
      "url": "/fs2-gtfs/documentation/model/",
      "content": "The GTFS data model The core library provides case classes encoding the entity as described in the GTFS standard. These classes can be used to read and write GTFS data when you are dealing with files respecting the standard. The entities are typed and can be used safely for transformations. GTFS Routes Routes have a field indicating their type. The GTFS standard defines only 13 of them, but there is an extension for more fine-grained route types. The default model provided by the library allows for parameterizing routes with the route type type. One can use Int to get the raw type, but it also defines enumerations for types. Depending on the type used by the GTFS files, you can read routes using: // use this if your GTFS file uses the simple GTFS route types. This is an alias for `Route[SimpleRouteType]` gtfs.read.routes[SimpleRoute] // use this if your GTFS file uses the extended GTFS route types. This is an alias for `Route[ExtendedRouteType]` gtfs.read.routes[ExtendedRoute] // use this if your GTFS file uses both the simple and the extended GTFS route types. This is an alias for `Route[Either[SimpleRouteType, ExtendedRouteType]]` gtfs.read.routes[EitherRoute]"
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
