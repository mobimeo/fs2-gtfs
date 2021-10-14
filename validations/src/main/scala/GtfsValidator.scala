package src.main.scala

import cats.data.ValidatedNel
import fs2.data.csv.CsvRow

case class Problem(desc: String)

trait GtfsValidator {

  def validate(row: CsvRow[String]): ValidatedNel[Problem, CsvRow[String]]

}
