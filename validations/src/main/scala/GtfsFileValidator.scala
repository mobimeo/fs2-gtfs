package src.main.scala

import com.mobimeo.gtfs.file.GtfsFile
import fs2._

case class GtfsProblem(fileName: String, row: Long, problem: Problem)

class GtfsFileValidator[F[_]](validations: Map[String, GtfsValidator]) {

  def validate(gtfsFile: GtfsFile[F]) = {
    Stream.emits(validations.toSeq).flatMap {
      case (fileName, validator) =>
        gtfsFile.read.rawFile(fileName).zipWithIndex
          .through(new GtfsValidatorPipe(validator))
          .map {
            case (problem, index) => GtfsProblem(fileName, index, problem)
          }
    }
  }
}
