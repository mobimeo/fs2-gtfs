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

package com.mobimeo.gtfs.model

import fs2.data.csv.*
import fs2.data.csv.generic.semiauto.*
import java.time.*


class SecondsSinceMidnight(val seconds: Int) extends AnyVal {
  def toLocalTime: LocalTime = LocalTime.ofSecondOfDay(seconds.toLong % (3600L * 24))
}

object SecondsSinceMidnight {
  private val TimePattern = raw"(-?\d+):(\d{1,2}):(\d{1,2})".r
  given CellDecoder[SecondsSinceMidnight] =
    CellDecoder.stringDecoder.emap {
      case TimePattern(hours, minutes, seconds) =>
        Right(new SecondsSinceMidnight(hours.toInt * 3600 + minutes.toInt * 60 + seconds.toInt))
      case s =>
        Left(new DecoderError(s"Invalid time '$s'"))
    }

  given CellEncoder[SecondsSinceMidnight] =
    CellEncoder.stringEncoder.contramap { seconds =>
      val hours   = seconds.seconds / 3600
      val minutes = (math.abs(seconds.seconds) % 3600) / 60
      val secs    = math.abs(seconds.seconds)  % 60
      f"$hours%02d:$minutes%02d:$secs%02d"
    }
}
