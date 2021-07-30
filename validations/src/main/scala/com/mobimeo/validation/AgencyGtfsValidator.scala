package src.main.scala.com.mobimeo.validation

import cats.syntax.all._
import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.com.mobimeo.{GtfsValidator, Problem}

class AgencyGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("agency_id").isDefined, row, Problem("Agency ID is required")),
      Validated.condNel(row.apply("agency_name").isDefined, row, Problem("Agency Name is required"))
      ).mapN((_, _) => row)
  }
}
