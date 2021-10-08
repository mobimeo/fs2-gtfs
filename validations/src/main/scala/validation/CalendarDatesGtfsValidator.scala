package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class CalendarDatesGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("service_id").isDefined, row, Problem("Service ID is required")),
      Validated.condNel(row.apply("date").isDefined, row, Problem("Date is required")),
      Validated.condNel(row.apply("exception_type").isDefined, row, Problem("Exception type is required"))
      ).mapN((_, _, _) => row)
  }
}
