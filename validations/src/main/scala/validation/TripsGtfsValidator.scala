package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class TripsGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("route_id").isDefined, row, Problem("Route ID is required")),
      Validated.condNel(row.apply("service_id").isDefined, row, Problem("Service ID is required")),
      Validated.condNel(row.apply("trip_id").isDefined, row, Problem("Trip ID is required")),
      ).mapN((_, _, _) => row)
  }
}
