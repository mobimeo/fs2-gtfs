package test

import cats.effect._
import cats.syntax.all._
import com.mobimeo.gtfs.file._
import java.nio.file.Paths

object ListStopNames extends IOApp {
  def run(args: List[String]) =
    args match {
      case Nil => IO(println("please provide the path to the GTFS file as argument")).as(ExitCode.Error)
      case path :: _ =>
        GtfsFile[IO](Paths.get(path))
          .use { gtfs =>
            gtfs.read.rawStops.map(stop => stop("stop_name").filter(_.nonEmpty)).unNone.compile.toList
          }
          .flatTap(stops => IO.println(stops.mkString(" - ", "\n - ", "")))
          .as(ExitCode.Success)
    }
}
