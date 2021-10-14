package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class StopTimesGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("trip_id").isDefined, row, Problem("Trip ID is required")),
      Validated.condNel(row.apply("stop_id").isDefined, row, Problem("Stop ID is required")),
      Validated.condNel(row.apply("stop_sequence").isDefined, row, Problem("Stop sequence is required")),
      ).mapN((_, _, _) => row)
  }
}
