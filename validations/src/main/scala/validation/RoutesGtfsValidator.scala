package src.main.scala.validation

import cats.syntax.all._
import cats.data.{Validated, ValidatedNel}
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class RoutesGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("route_id").isDefined, row, Problem("Route ID is required")),
      Validated.condNel(row.apply("route_type").isDefined, row, Problem("Route type is required"))
      ).mapN((_, _) => row)
  }
}
