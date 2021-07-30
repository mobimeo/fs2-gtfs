package src.main.scala.com.mobimeo

import fs2._
import cats.data.Validated
import fs2.data.csv.CsvRow

class GtfsValidatorPipe[F[_]](validator: GtfsValidator) extends Pipe[F, (CsvRow[String], Long), (Problem, Long)] {

  override def apply(rows: Stream[F, (CsvRow[String], Long)]): Stream[F, (Problem, Long)] =
    rows.flatMap {
      case (row, idx) =>
        validator.validate(row) match {
          case Validated.Valid(_) => Stream.empty
          case Validated.Invalid(e) => Stream.emits(e.toList).map(_ -> idx)
        }
    }
}
