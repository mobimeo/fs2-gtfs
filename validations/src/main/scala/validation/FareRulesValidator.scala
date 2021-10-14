package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class FareRulesValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    Validated.condNel(row.apply("fare_id").isDefined, row, Problem("Fare ID is required"))
  }
}
