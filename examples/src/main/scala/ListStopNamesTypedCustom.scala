package test

import cats.effect._
import cats.syntax.all._
import com.mobimeo.gtfs.file._
import fs2.data.csv._
import fs2.data.csv.generic._
import fs2.data.csv.generic.semiauto._
import java.nio.file.Paths

case class StopName(@CsvName("stop_name") name: Option[String])
object StopName {
  implicit val decoder: CsvRowDecoder[StopName, String] = deriveCsvRowDecoder
}

object ListStopNamesTypedCustom extends IOApp {
  def run(args: List[String]) =
    args match {
      case Nil => IO(println("please provide the path to the GTFS file as argument")).as(ExitCode.Error)
      case path :: _ =>
        GtfsFile[IO](Paths.get(path))
          .use { gtfs =>
            gtfs.read.stops[StopName].collect { case StopName(Some(name)) => name }.compile.toList
          }
          .flatTap(stops => IO.println(stops.mkString(" - ", "\n - ", "")))
          .as(ExitCode.Success)
    }
}
