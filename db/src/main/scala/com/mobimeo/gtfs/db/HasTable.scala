package com.mobimeo.gtfs.db

import com.mobimeo.gtfs.StandardName
import com.mobimeo.gtfs.model._

trait HasTable[T] {
  def tableName: String
  def columns: List[ColumnDefinition]
}

case class ColumnDefinition(name: String, `type`: String, modifiers: List[String]) {
    override def toString: String = s"$name ${`type`} ${modifiers.mkString(" ")}"
}

object HasTable {
  def instance[A](name: String)(cols: ColumnDefinition*) =
    new HasTable[A] {
      def tableName: String               = GtfsDb.normalizeTableName(name)
      def columns: List[ColumnDefinition] = cols.toList
    }

  def text(name: String, modifiers: String*): ColumnDefinition =
    ColumnDefinition(name, "text", modifiers.toList)

  def int(name: String, modifiers: String*): ColumnDefinition =
    ColumnDefinition(name, "int", modifiers.toList)

  val primaryKey = "primary key"
  val notNull    = "not null"

  implicit val stopHasTable: HasTable[Stop] = {
    import tables.Stop._
    HasTable.instance(StandardName.Stops.entryName)(
      text(id, primaryKey),
      text(code),
      text(name),
      text(desc),
      text(lat),
      text(lon),
      text(zoneId),
      text(url),
      int(locationType),
      text(parentStation),
      text(timezone),
      text(wheelchairBoarding),
      text(levelId),
      text(platformCode)
    )
  }

  implicit val transferHasTable: HasTable[Transfer] = {
    import tables.Transfer._
    HasTable.instance(StandardName.Transfers.entryName)(
      text(fromStopId, notNull),
      text(toStopId, notNull),
      int(transferType, notNull),
      int(minTransferTime)
    )
  }

  implicit val agencyHasTable: HasTable[Agency] = {
    import tables.Agency._
    HasTable.instance(StandardName.Agency.entryName)(
      text(id, primaryKey),
      text(name, notNull),
      text(url, notNull),
      text(timezone, notNull),
      text(language),
      text(phone),
      text(fareUrl),
      text(email)
    )
  }
}
