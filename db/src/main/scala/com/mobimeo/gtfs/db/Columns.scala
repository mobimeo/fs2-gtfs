package com.mobimeo.gtfs.db

trait Columns {
  sealed abstract class ColumnImpl {
    type Type[A] <: String
    def apply[A](s: String): Type[A]
  }

  val Column: ColumnImpl =
    new ColumnImpl {
      type Type[A] = String
      def apply[A](s: String): Type[A] = s
    }
}
