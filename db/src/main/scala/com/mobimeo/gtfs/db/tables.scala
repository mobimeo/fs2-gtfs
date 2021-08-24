package com.mobimeo.gtfs.db

import com.mobimeo.gtfs.model._

object tables {

  object Stop {

    val id                 = Column[Stop]("stop_id")
    val code               = Column[Stop]("stop_code")
    val name               = Column[Stop]("stop_name")
    val desc               = Column[Stop]("stop_desc")
    val lat                = Column[Stop]("stop_lat")
    val lon                = Column[Stop]("stop_lon")
    val zoneId             = Column[Stop]("zone_id")
    val url                = Column[Stop]("stop_url")
    val locationType       = Column[Stop]("location_type")
    val parentStation      = Column[Stop]("parent_station")
    val timezone           = Column[Stop]("stop_timezone")
    val wheelchairBoarding = Column[Stop]("wheelchair_boarding")
    val levelId            = Column[Stop]("level_id")
    val platformCode       = Column[Stop]("platform_code")

  }

  object Transfer {

    val fromStopId      = Column[Transfer]("from_stop_id")
    val toStopId        = Column[Transfer]("to_stop_id")
    val transferType    = Column[Transfer]("transfer_type")
    val minTransferTime = Column[Transfer]("min_transfer_time")

  }

  object Agency {

    val id       = Column[Agency]("agency_id")
    val name     = Column[Agency]("agency_name")
    val url      = Column[Agency]("agency_url")
    val timezone = Column[Agency]("agency_timezone")
    val language = Column[Agency]("agency_lang")
    val phone    = Column[Agency]("agency_phone")
    val fareUrl  = Column[Agency]("agency_fare_url")
    val email    = Column[Agency]("agency_email")

  }

}
