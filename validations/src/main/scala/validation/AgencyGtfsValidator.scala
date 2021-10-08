package src.main.scala.validation

import cats.syntax.all._
import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class AgencyGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("agency_id").isDefined, row, Problem("Agency ID is required")),
      Validated.condNel(row.apply("agency_name").isDefined, row, Problem("Agency Name is required")),
      Validated.condNel(row.apply("agency_url").isDefined, row, Problem("Agency URL is required")),
      Validated.condNel(row.apply("agency_timezone").isDefined, row, Problem("Agency Timezone is required"))
      ).mapN((_, _, _, _) => row)
  }
}
