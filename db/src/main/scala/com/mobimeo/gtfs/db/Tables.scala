package com.mobimeo.gtfs.db

import doobie.implicits._
import com.mobimeo.gtfs.model

object Tables {

  object Agency {
      val create = sql"""
      |create table agency (
      |  agency_id text primary key,
      |  agency_name text,
      |  agency_url text,
      |  agency_timezone text,
      |  agency_lang text,
      |  agency_phone text,
      |  agency_fare_url text,
      |  email text    
      )""".stripMargin

      def insert(agency: model.Agency) = sql"""
      |insert into agency (
      |  agency_id,
      |  agency_name,
      |  agency_url,
      |  agency_timezone,
      |  agency_lang,
      |  agency_phone,
      |  agency_fare_url,
      |  email
      |) values (
      |  ${agency.id},
      |  ${agency.name},
      |  ${agency.url},
      |  ${agency.timezone},
      |  ${agency.language},
      |  ${agency.phone},
      |  ${agency.fareUrl},
      |  ${agency.email}
      |)""".stripMargin

      def selectById(agencyId: String) = sql"""
      |select agency_id,
      |  agency_name,
      |  agency_url,
      |  agency_timezone,
      |  agency_lang,
      |  agency_phone,
      |  agency_fare_url,
      |  email
      |from agency where agency_id = $agencyId
      |""".stripMargin 
  }
}
