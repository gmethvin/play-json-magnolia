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

import java.time.LocalDate

import org.scalatest._
import play.api.libs.json._

class MagnoliaJsonSpec extends WordSpec with MustMatchers {

  import TestData._

  "MagnoliaJson" should {
    "derive Reads and Writes for a case class" in {

      implicit lazy val reads = MagnoliaReads.gen[Employee]
      implicit lazy val writes = MagnoliaWrites.gen[Employee]

      val emp1 = Employee("Test", "Tester", LocalDate.parse("1982-10-08"), Some(Department.Engineering))
      val json1 = Json.parse("""
         |{
         |   "firstName": "Test",
         |   "lastName": "Tester",
         |   "birthDate": "1982-10-08",
         |   "department": { "_type": "Engineering" }
         |}
       """.stripMargin)
      val fromJson1 = Json.fromJson(json1)
      val toJson1 = Json.toJson(emp1)

      fromJson1 mustBe JsSuccess(emp1)
      toJson1 mustBe json1

      val emp2 =
        Employee("Foo", "Bar", LocalDate.parse("1982-10-08"), None)
      val json2 = Json.parse("""
         |{
         |   "firstName": "Foo",
         |   "lastName": "Bar",
         |   "birthDate": "1982-10-08"
         |}
       """.stripMargin)
      val fromJson2 = Json.fromJson(json2)
      val toJson2 = Json.toJson(emp2)

      fromJson2 mustBe JsSuccess(emp2)
      toJson2 mustBe json2
    }
  }
}

private object TestData {
  case class Employee(firstName: String, lastName: String, birthDate: LocalDate, department: Option[Department])
  sealed trait Department
  object Department {
    case object Engineering extends Department
    case object CustomerSuccess extends Department
    case object Sales extends Department
    case object Finance extends Department
  }
}
