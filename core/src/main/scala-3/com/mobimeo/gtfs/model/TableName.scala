package com.mobimeo.gtfs.model

enum TableName(val entryName: String) extends EnumEntry {
  case Agency       extends TableName("agency")
  case Stops        extends TableName("stops")
  case Routes       extends TableName("routes")
  case Trips        extends TableName("trips")
  case StopTimes    extends TableName("stop_times")
  case FeedInfo     extends TableName("feed_info")
  case Pathways     extends TableName("pathways")
  case Levels       extends TableName("levels")
  case Attributions extends TableName("attributions")
}

object TableName extends CsvEnum[TableName]
