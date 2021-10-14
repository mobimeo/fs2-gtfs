package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class CalendarGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("service_id").isDefined, row, Problem("Service ID is required")),
      Validated.condNel(row.apply("monday").isDefined, row, Problem("Monday is required")),
      Validated.condNel(row.apply("tuesday").isDefined, row, Problem("Tuesday is required")),
      Validated.condNel(row.apply("wednesday").isDefined, row, Problem("Wednesday is required")),
      Validated.condNel(row.apply("thursday").isDefined, row, Problem("Thursday is required")),
      Validated.condNel(row.apply("friday").isDefined, row, Problem("Friday is required")),
      Validated.condNel(row.apply("saturday").isDefined, row, Problem("Saturday is required")),
      Validated.condNel(row.apply("sunday").isDefined, row, Problem("Sunday is required")),
      Validated.condNel(row.apply("start_date").isDefined, row, Problem("Start date is required")),
      Validated.condNel(row.apply("end_date").isDefined, row, Problem("End date is required"))
      ).mapN((_, _, _, _, _, _, _, _, _, _) => row)
  }
}
