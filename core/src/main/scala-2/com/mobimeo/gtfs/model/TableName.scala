package com.mobimeo.gtfs.model

import enumeratum.{Enum, EnumEntry}

sealed trait TableName extends EnumEntry with EnumEntry.Snakecase
object TableName extends Enum[TableName] with CsvEnum[TableName] {
  case object Agency       extends TableName
  case object Stops        extends TableName
  case object Routes       extends TableName
  case object Trips        extends TableName
  case object StopTimes    extends TableName
  case object FeedInfo     extends TableName
  case object Pathways     extends TableName
  case object Levels       extends TableName
  case object Attributions extends TableName

  val values = findValues
}
