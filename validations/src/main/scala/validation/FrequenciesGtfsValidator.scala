package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class FrequenciesGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("trip_id").isDefined, row, Problem("Trip ID is required")),
      Validated.condNel(row.apply("start_time").isDefined, row, Problem("Start time is required")),
      Validated.condNel(row.apply("end_time").isDefined, row, Problem("End time is required")),
      Validated.condNel(row.apply("headway_secs").isDefined, row, Problem("Headway Secs is required"))
      ).mapN((_, _, _, _) => row)
  }
}
