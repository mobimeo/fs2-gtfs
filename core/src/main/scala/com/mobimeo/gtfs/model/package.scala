/*
 * Copyright 2021 Mobimeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobimeo.gtfs

import cats.syntax.all._

import fs2.data.csv.{CellDecoder, CellEncoder, DecoderError}

import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter
import java.{util => ju}

package object model {

  type ExtendedRoute = Route[ExtendedRouteType]

  type SimpleRoute = Route[SimpleRouteType]

  type EitherRoute = Route[Either[SimpleRouteType, ExtendedRouteType]]

  implicit val zoneIdCellDecoder: CellDecoder[ZoneId] =
    CellDecoder.stringDecoder.emap(s =>
      Either.catchNonFatal(ZoneId.of(s)).leftMap(t => new DecoderError(s"Invalid zone id $s", None, t))
    )

  implicit val zoneIdCellEncoder: CellEncoder[ZoneId] =
    CellEncoder.stringEncoder.contramap(_.getId())

  implicit val localDateDecoder: CellDecoder[LocalDate] =
    CellDecoder.stringDecoder.emap(s =>
      Either
        .catchNonFatal(LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE))
        .leftMap(t => new DecoderError(s"Invalid date $s", None, t))
    )

  implicit val localDateEncoder: CellEncoder[LocalDate] =
    CellEncoder.stringEncoder.contramap(_.format(DateTimeFormatter.BASIC_ISO_DATE))

  implicit val currencyDecoder: CellDecoder[ju.Currency] =
    CellDecoder.stringDecoder.emap(s =>
      Either.catchNonFatal(ju.Currency.getInstance(s)).leftMap(t => new DecoderError(s"Invalid currency: $s", None, t))
    )

  implicit val boolDecoder: CellDecoder[Boolean] =
    CellDecoder.intDecoder.emap {
      case 0 => Right(false)
      case 1 => Right(true)
      case n => Left(new DecoderError(s"Invalid boolean $n"))
    }

  implicit val boolEncoder: CellEncoder[Boolean] =
    CellEncoder.intEncoder.contramap(if (_) 1 else 0)

  implicit val currencyEncoder: CellEncoder[ju.Currency] =
    CellEncoder.stringEncoder.contramap(_.getCurrencyCode())

  implicit val localeDecoder: CellDecoder[ju.Locale] =
    CellDecoder.stringDecoder.map(new ju.Locale(_))

  implicit val localeEncoder: CellEncoder[ju.Locale] =
    CellEncoder.stringEncoder.contramap(_.getISO3Language())

  implicit def eitherCellDecoder[A, B](implicit A: CellDecoder[A], B: CellDecoder[B]): CellDecoder[Either[A, B]] =
    A.either(B)

  implicit def eitherCellEncoder[A, B](implicit A: CellEncoder[A], B: CellEncoder[B]): CellEncoder[Either[A, B]] =
    _.fold(A(_), B(_))

}
