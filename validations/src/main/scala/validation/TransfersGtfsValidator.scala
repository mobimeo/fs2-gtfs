package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class TransfersGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("from_stop_id").isDefined, row, Problem("From stop ID is required")),
      Validated.condNel(row.apply("to_stop_id").isDefined, row, Problem("To stop ID is required")),
      Validated.condNel(row.apply("transfer_type").isDefined, row, Problem("Transfer type is required")),
      ).mapN((_, _, _) => row)
  }
}
