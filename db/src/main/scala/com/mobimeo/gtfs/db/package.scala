package com.mobimeo.gtfs

import doobie.*
import doobie.postgres.pgisgeographyimplicits.*
import java.net.URL
import java.time.*
import org.postgis.Point

package object db:
  given Meta[model.Availability]        = Meta[Boolean].imap {
                                            if (_) model.Availability.Availabile
                                            else   model.Availability.Unavailable
                                          } {
                                            case model.Availability.Availabile   => true
                                            case model.Availability.Unavailable  => false
                                          }
  given Meta[URL]                       = Meta[String].imap(new URL(_))(_.toString)
  given Meta[ZoneId]                    = Meta[String].imap(ZoneId.of)(_.getId)
  given Meta[model.Coordinate]          = Meta[Point].imap(p => model.Coordinate(lon = p.x, lat = p.y))(c => new Point(c.lon, c.lat))
  given Meta[model.ExtendedRouteType]   = Meta[String].imap(model.ExtendedRouteType.valueOf)(_.toString)
  given Meta[model.ExceptionType]       = Meta[String].imap(model.ExceptionType.valueOf)(_.toString)
  given Meta[model.LocationType]        = Meta[String].imap(model.LocationType.valueOf)(_.toString)
  given Meta[model.PickupOrDropOffType] = Meta[String].imap(model.PickupOrDropOffType.valueOf)(_.toString)
  given Meta[model.Timepoint]           = Meta[String].imap(model.Timepoint.valueOf)(_.toString)
  given Meta[model.TransferType]        = Meta[String].imap(model.TransferType.valueOf)(_.toString)
