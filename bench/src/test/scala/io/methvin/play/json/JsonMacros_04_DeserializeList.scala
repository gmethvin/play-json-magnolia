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
import org.openjdk.jmh.annotations._

@State(Scope.Benchmark)
class JsonMacros_04_DeserializeList {

  var employees: Seq[Employee] = _
  var employeesJson: JsValue = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    employeesJson = JsArray((1 to 100) map { id =>
      Json.obj(
        "employeeNumber" -> id,
        "firstName" -> s"Foo$id",
        "lastName" -> s"Bar$id",
        "city" -> "New York",
        "country" -> "United States",
        "tags" -> Seq("a", "b", "c")
      )
    })
  }

  @TearDown(Level.Iteration)
  def tearDown(): Unit = {}

  @Benchmark
  def fromJsonPlayMacro(): Seq[Employee] = {
    employees = Json.fromJson[Seq[Employee]](employeesJson).get
    employees
  }

  @Benchmark
  def fromJsonMagnoliaMacro(): Seq[Employee] = {
    implicit val reads: Reads[Employee] = MagnoliaReads.gen[Employee]
    employees = Json.fromJson[Seq[Employee]](employeesJson).get
    employees
  }

}
