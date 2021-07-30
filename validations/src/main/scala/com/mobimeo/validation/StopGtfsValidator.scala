package src.main.scala.com.mobimeo.validation

import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.com.mobimeo.{GtfsValidator, Problem}

class StopGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    Validated.condNel(row.apply("stop_id").isDefined, row, Problem("Stop ID is required"))
  }
}
