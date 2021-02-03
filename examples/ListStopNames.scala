package test

import cats.effect._
import cats.implicits._
import com.mobimeo.gtfs._
import java.nio.file.Paths

object ListStopNames extends IOApp {
  def run(args: List[String]) =
    args match {
      case Nil => IO(println("please provide the path to the GTFS file as argument")).as(ExitCode.Error)
      case path :: _ =>
        Blocker[IO]
          .use { blocker =>
            Gtfs[IO](Paths.get(path), blocker).use { gtfs =>
              gtfs.read.rawStops.map(stop => stop("stop_name").filter(_.nonEmpty)).unNone.compile.toList
            }
          }
          .flatTap(stops => IO(println(stops.mkString(" - ", "\n - ", ""))))
          .as(ExitCode.Success)
    }
}
