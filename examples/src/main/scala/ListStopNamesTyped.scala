import cats.effect._
import cats.syntax.all._
import com.mobimeo.gtfs.file._
import com.mobimeo.gtfs.model._
import java.nio.file.Paths

object ListStopNamesTyped extends IOApp {
  def run(args: List[String]) =
    args match {
      case Nil => IO(println("please provide the path to the GTFS file as argument")).as(ExitCode.Error)
      case path :: _ =>
        GtfsFile[IO](Paths.get(path))
          .use { gtfs =>
            gtfs.read.stops[Stop].map(_.name.filter(_.nonEmpty)).unNone.compile.toList
          }
          .flatTap(stops => IO.println(stops.mkString(" - ", "\n - ", "")))
          .as(ExitCode.Success)
    }
}
