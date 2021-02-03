package test

import cats.effect._
import cats.implicits._
import com.mobimeo.gtfs._
import java.nio.file.{Paths, StandardCopyOption}

object AllCapsStops extends IOApp {
  def run(args: List[String]) =
    args match {
      case origin :: target :: _ =>
        Blocker[IO]
          .use { blocker =>
            Gtfs[IO](Paths.get(origin), blocker).use { gtfs =>
              gtfs.copyTo(Paths.get(target), Seq(StandardCopyOption.REPLACE_EXISTING)).use { target =>
                gtfs.read.rawStops
                  .map(_.modify("stop_name")(_.toUpperCase))
                  .through(target.write.rawStops)
                  .compile
                  .toList
              }
            }
          }
          .flatTap(stops => IO(println(stops.mkString(" - ", "\n - ", ""))))
          .as(ExitCode.Success)
      case _ => IO(println("please provide the paths to the GTFS files as arguments")).as(ExitCode.Error)
    }
}
