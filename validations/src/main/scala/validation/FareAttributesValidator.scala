package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class FareAttributesValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("fare_id").isDefined, row, Problem("Fare ID is required")),
      Validated.condNel(row.apply("price").isDefined, row, Problem("Price is required")),
      Validated.condNel(row.apply("currency_type").isDefined, row, Problem("Currency type is required")),
      Validated.condNel(row.apply("payment_method").isDefined, row, Problem("Payment method is required")),
      Validated.condNel(row.apply("transfers").isDefined, row, Problem("Transfers is required"))
      ).mapN((_, _, _, _, _) => row)
  }
}
