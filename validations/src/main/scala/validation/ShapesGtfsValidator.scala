package src.main.scala.validation

import cats.data.{Validated, ValidatedNel}
import cats.syntax.all._
import fs2.data.csv.CsvRow
import src.main.scala.{GtfsValidator, Problem}

class ShapesGtfsValidator extends GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]] = {
    (Validated.condNel(row.apply("shape_id").isDefined, row, Problem("Shape ID is required")),
      Validated.condNel(row.apply("shape_pt_lat").isDefined, row, Problem("Shape lat is required")),
      Validated.condNel(row.apply("shape_pt_lon").isDefined, row, Problem("Shape lon type is required")),
      Validated.condNel(row.apply("shape_pt_sequence").isDefined, row, Problem("Shape sequence is required"))
      ).mapN((_, _, _, _) => row)
  }
}
