/*
 * Copyright 2018 Greg Methvin
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

package io.methvin.play.json

import magnolia._
import play.api.libs.json._

import scala.collection.mutable
import scala.language.experimental.macros

object MagnoliaJson {
  final val TypeProperty = "_type"
  final val ValueProperty = "_value"
}

object MagnoliaReads {
  type Typeclass[T] = Reads[T]

  def combine[T](ctx: CaseClass[Reads, T]): Reads[T] = {
    if (classOf[Some[_]].getName == ctx.typeName.full) {
      // This is a small hack to hard-code the behavior for Option.
      // Magnolia gives us the typeclass for the parameter of the Some, so we wrap that in OptionReads.
      // Then we later obtain that OptionReads in dispatch.
      // This allows us to check for the OptionReads to see if we're dealing with an Option.
      new OptionReads(ctx.parameters.head.typeclass).asInstanceOf[Reads[T]]
    } else {
      new Reads[T] {
        override def reads(json: JsValue): JsResult[T] = {
          try {
            JsSuccess(ctx.construct { param =>
              import param.{default, typeclass}
              (json \ param.label, default) match {
                case (JsUndefined(), Some(dv)) => dv
                case (JsUndefined(), None) if typeclass.isInstanceOf[OptionReads[_]] =>
                  // Map nonexistent fields to None
                  None
                case (lookup, _) => lookup.as(typeclass)
              }
            })
          } catch {
            // TODO: maybe don't rely on exceptions here?
            // Using exceptions gives us about a 3x speedup when deserializing vs using a fold
            case e: JsResultException => JsError(e.errors)
          }
        }
      }
    }
  }

  def dispatch[T](ctx: SealedTrait[Reads, T]): Reads[T] = {
    if (classOf[Option[_]].getName == ctx.typeName.full) {
      ctx.subtypes
        .find(_.typeclass.isInstanceOf[OptionReads[_]])
        .get
        .typeclass
        .asInstanceOf[Reads[T]]
    } else {
      new Reads[T] {
        override def reads(json: JsValue): JsResult[T] = {
          (json \ MagnoliaJson.TypeProperty).validate[String].flatMap { typeName =>
            ctx.subtypes.find(typeName == _.typeName.short) match {
              case Some(subtype) =>
                val tc = subtype.typeclass
                // Try to read object with embedded _type then try reading value from _value
                tc.reads(json)
                  .orElse((json \ MagnoliaJson.ValueProperty).validate(tc))
              case None => JsError("error.expected.typename")
            }
          }
        }
      }
    }
  }

  class OptionReads[T](rds: Reads[T]) extends Reads[Option[T]] {
    override def reads(json: JsValue): JsResult[Option[T]] = json match {
      case JsNull => JsSuccess(None)
      case jsv => rds.reads(jsv).map(Some(_))
    }
  }

  implicit def gen[T]: Reads[T] = macro Magnolia.gen[T]
}

object MagnoliaWrites {
  type Typeclass[T] = Writes[T]

  def combine[T](ctx: CaseClass[Writes, T]): Writes[T] =
    if (classOf[Some[_]].getName == ctx.typeName.full) {
      new OptionWrites(ctx.parameters.head.typeclass).asInstanceOf[Writes[T]]
    } else {
      new Writes[T] {
        override def writes(o: T): JsValue = {
          // Building a mutable map gives us a significant perf improvement
          val resultMap = new mutable.LinkedHashMap[String, JsValue]
          ctx.parameters.foreach { p =>
            val tc = p.typeclass
            val value = p.dereference(o)
            if (value != None) { // ignore values of None
              resultMap.put(p.label, tc.writes(value))
            }
          }
          JsObject(resultMap)
        }
      }
    }

  def dispatch[T](ctx: SealedTrait[Writes, T]): Writes[T] = {
    if (classOf[Option[_]].getName == ctx.typeName.full) {
      ctx.subtypes
        .find(_.typeclass.isInstanceOf[OptionWrites[_]])
        .get
        .typeclass
        .asInstanceOf[Writes[T]]
    } else {
      new Writes[T] {
        override def writes(o: T): JsValue = {
          ctx.dispatch(o) { subtype =>
            val typeField = MagnoliaJson.TypeProperty -> JsString(subtype.typeName.short)
            subtype.typeclass.writes(subtype.cast(o)) match {
              case JsObject(fields) => JsObject(fields + typeField)
              case jsv =>
                JsObject(Seq(typeField, MagnoliaJson.ValueProperty -> jsv))
            }
          }
        }
      }
    }
  }

  class OptionWrites[T](wts: Writes[T]) extends Writes[Option[T]] {
    override def writes(o: Option[T]): JsValue = o match {
      case Some(v) => wts.writes(v)
      case None => JsNull
    }
  }

  implicit def gen[T]: Writes[T] = macro Magnolia.gen[T]
}
