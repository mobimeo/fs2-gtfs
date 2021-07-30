package src.main.scala.com.mobimeo

import com.mobimeo.gtfs.file.GtfsFile
import fs2._

case class GtfsProblem(fileName: String, row: Long, problem: Problem)

class GtfsFileValidator[F[_]](validations: Map[String, GtfsValidator]) {

  def validate(gtfsFile: GtfsFile[F]) = {
    Stream.emits(validations.toSeq).flatMap {
      case (name, validator) =>
        gtfsFile.read.rawFile(name).zipWithIndex
          .through(new GtfsValidatorPipe(validator))
          .map {
            case (p, i) => GtfsProblem(name, i, p)
          }
    }
  }
}
