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

import play.api.libs.json._

case class Employee(
  employeeNumber: Int,
  firstName: String,
  lastName: String,
  city: String,
  country: String,
  tags: Seq[String]
)

object Employee {
  implicit val employeeFormat: Format[Employee] = Json.format[Employee]

  val magnoliaWrites: Writes[Employee] = MagnoliaWrites.gen[Employee]

  val magnoliaReads: Reads[Employee] = MagnoliaReads.gen[Employee]
}
