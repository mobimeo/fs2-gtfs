package com.mobimeo.gtfs.db

import doobie._
import scala.annotation.unused
import shapeless._
import shapeless.labelled.FieldType
import fs2.data.csv.generic.CsvName
import com.mobimeo.gtfs.StandardName

object Tables {

  case class ColumnHeading(name: String, typ: ColumnType[_])
  case class ColumnType[A](value: String, modifiers: List[String])

  object ColumnType {
    implicit def putOptType[A](implicit put: Put[A]): ColumnType[Option[A]] =
      ColumnType(put.jdbcTargets.head.toString.toLowerCase, Nil)
    implicit def putType[A](implicit put: Put[A]): ColumnType[A] =
      ColumnType(put.jdbcTargets.head.toString.toLowerCase, List("not null"))
  }

  case class RelationHeading[A](columns: List[ColumnHeading])

  trait TableSchema[A] {
    
    val columns: List[ColumnHeading]

    def create(name: StandardName) = Fragment.const(
      columns
        .map {
          case ColumnHeading(col, ColumnType(value, modifiers)) =>
            s"  ${GtfsDb.quoteFieldName(col)} $value${modifiers.mkString(" ", " ", "")}"
        }
        .mkString(s"create table ${GtfsDb.normalizeTableName(name.entryName)} (\n", ",\n", "\n);")
    )

  }

  case class DerivedTableSchema[A](columns: List[ColumnHeading]) extends TableSchema[A]

  object TableSchema {
    def apply[A](implicit ev: DerivedTableSchema[A]): TableSchema[A] = ev
  }

  trait AnnotatedRelationHeading[Repr, Anno] {
    def heading(annotations: Anno): RelationHeading[Repr]
  }

  object AnnotatedRelationHeading {
    def apply[A, Repr, AnnoRepr](implicit ev: AnnotatedRelationHeading[Repr, AnnoRepr]) = ev
  }

  implicit def hconsHeading[Key <: Symbol, Head, Tail <: HList, AnnoHead, AnnoTail <: HList](implicit
      witness: Witness.Aux[Key],
      hColumn: Lazy[ColumnType[Head]],
      tSchema: AnnotatedRelationHeading[Tail, AnnoTail],
      subtype: AnnoHead <:< Option[CsvName]
  ): AnnotatedRelationHeading[FieldType[Key, Head] :: Tail, AnnoHead :: AnnoTail] =
    new AnnotatedRelationHeading[FieldType[Key, Head] :: Tail, AnnoHead :: AnnoTail] {
      def heading(annotations: AnnoHead :: AnnoTail): RelationHeading[FieldType[Key, Head] :: Tail] = {
        val typ  = hColumn.value
        val name = annotations.head.fold(witness.value.name)(_.name)
        RelationHeading(ColumnHeading(name, typ) :: tSchema.heading(annotations.tail).columns)
      }
    }

  implicit val hnilHeading: AnnotatedRelationHeading[HNil, HNil] =
    new AnnotatedRelationHeading[HNil, HNil] {
      def heading(annotations: HNil): RelationHeading[HNil] = RelationHeading(Nil)
    }

  implicit def genericSchema[A, Repr, Anno <: HList](implicit      
      @unused generic: LabelledGeneric.Aux[A, Repr],
      annotations: Annotations.Aux[CsvName, A, Anno],
      hSchema: Lazy[AnnotatedRelationHeading[Repr, Anno]]
  ): DerivedTableSchema[A] =
    DerivedTableSchema(hSchema.value.heading(annotations()).columns)

}
