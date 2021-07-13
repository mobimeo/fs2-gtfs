package test

import cats.effect._
import cats.syntax.all._
import com.mobimeo.gtfs.file._
import java.nio.file.{Paths, StandardCopyOption}

object AllCapsStops extends IOApp {
  def run(args: List[String]) =
    args match {
      case origin :: target :: _ =>
        GtfsFile[IO](Paths.get(origin))
          .use { gtfs =>
            gtfs.copyTo(Paths.get(target), Seq(StandardCopyOption.REPLACE_EXISTING)).use { target =>
              gtfs.read.rawStops
                .map(_.modify("stop_name")(_.toUpperCase))
                .through(target.write.rawStops)
                .compile
                .toList
            }
          }
          .flatTap(stops => IO.println(stops.mkString(" - ", "\n - ", "")))
          .as(ExitCode.Success)
      case _ => IO(println("please provide the paths to the GTFS files as arguments")).as(ExitCode.Error)
    }
}
