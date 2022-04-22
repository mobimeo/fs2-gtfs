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

package com.mobimeo.gtfs.rules

import cats.data.NonEmptyList
import cats.syntax.all._
import io.circe.Decoder

/** A rule context is a trie map of strings. It allows for providing scoped values that can be used in the rules.
  */
sealed trait Ctx {

  /** Retrieves the value at the given path if any */
  def get(path: NonEmptyList[String]): Option[String] =
    get(path.toList)

  /** Retrieves the value at the given path if any */
  def get(path: List[String]): Option[String]

  /** Adds a value at the given key. This function replaces any existing value or sub tree at this path.
    */
  def set(path: NonEmptyList[String], value: String): Ctx = {
    def loop(path: NonEmptyList[String], ctx: Ctx): Ctx =
      NonEmptyList.fromList(path.tail) match {
        case None =>
          // add a leaf
          ctx match {
            case Ctx.Node(children) => Ctx.Node(children.updated(path.head, Ctx.Value(value)))
            case Ctx.Value(_)       => Ctx.Node(Map(path.head -> Ctx.Value(value)))
          }
        case Some(tail) =>
          // add sub-tree, adding keys along the path
          ctx match {
            case Ctx.Node(children) =>
              Ctx.Node(children.updated(path.head, loop(tail, children.getOrElse(path.head, Ctx.empty))))
            case Ctx.Value(_) =>
              Ctx.Node(Map(path.head -> loop(tail, Ctx.empty)))
          }
      }
    loop(path, this)
  }

}

object Ctx {

  object syntax {
    implicit class Ops(val s: String) extends AnyVal {
      def :=(v: String): (String, Ctx) = (s, Ctx.Value(v))
      def :=(c: Ctx): (String, Ctx)    = (s, c)
    }
  }

  val empty: Ctx = Node(Map.empty)

  def apply(bindings: (String, Ctx)*): Ctx =
    Node(bindings.toMap)

  implicit def decoder: Decoder[Ctx] =
    Decoder.decodeString
      .map[Ctx](Value(_)) or
      (j =>
        Decoder.decodeJsonObject(j).flatMap { o =>
          o.toList
            .traverse { case (k, v) =>
              v.as[Ctx](decoder).tupleLeft(k)
            }
            .map(l => apply(l: _*))
        }
      )

  private case class Node(children: Map[String, Ctx]) extends Ctx {
    def get(path: List[String]): Option[String] =
      path match {
        case h :: t => children.get(h).flatMap(_.get(t))
        case Nil    => None
      }
  }

  private case class Value(v: String) extends Ctx {
    def get(path: List[String]): Option[String] =
      path match {
        case Nil => Some(v)
        case _   => None
      }
  }

}
