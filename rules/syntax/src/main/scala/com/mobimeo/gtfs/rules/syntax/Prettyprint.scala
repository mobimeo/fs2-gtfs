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

package com.mobimeo.gtfs.rules.syntax

import cats.parse.{LocationMap, Parser}

class Prettyprint(input: String) {
  private val locmap = LocationMap(input)

  def description(x: Parser.Expectation): String = x match {
    case Parser.Expectation.OneOfStr(_, List(str)) =>
      s"expected $str"
    case Parser.Expectation.OneOfStr(_, strs) =>
      val strList = strs.map(x => s"'$x'").mkString(", ")
      s"expected one of $strList"
    case Parser.Expectation.InRange(_, lower, upper) =>
      if (lower == upper) s"expected '$lower'"
      else s"expected '$lower' ~ '$upper'"
    case Parser.Expectation.StartOfString(_) =>
      "expected beginning of file"
    case Parser.Expectation.EndOfString(_, _) =>
      "expected end of file"
    case Parser.Expectation.Length(_, expected, actual) =>
      s"unexpected eof; expected ${expected - actual} more characters"
    case Parser.Expectation.ExpectedFailureAt(_, matched) =>
      s"unexpected '$matched'"
    case Parser.Expectation.Fail(_) =>
      "failed to parse"
    case Parser.Expectation.FailWith(_, message) =>
      message
    case Parser.Expectation.WithContext(contextStr, _) =>
      s"expected $contextStr"
  }

  def prettyprint(x: Parser.Expectation): String = {
    val (row, col) = locmap.toLineCol(x.offset).getOrElse((0, input.size))
    val (r, c)     = (row + 1, col + 1)
    val line       = locmap.getLine(row).get
    val offending =
      s"${row.toString map { _ => ' ' }} | ${" " * col}^"
    s"""
      |$r:$c: error: ${description(x)}
      |$r | $line
      |$offending""".stripMargin
  }

  def prettyprint(x: Parser.Error): String =
    x.expected.map(prettyprint).toList.mkString("")
}
