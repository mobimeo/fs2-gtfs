package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class StopGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    Validated.condNel(row.apply("stop_id").isDefined, row, Problem("Stop ID is required"))
  }
}
