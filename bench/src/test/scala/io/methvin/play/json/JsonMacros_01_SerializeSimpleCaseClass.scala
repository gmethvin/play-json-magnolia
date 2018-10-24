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
class JsonMacros_01_SerializeSimpleCaseClass {

  var employee: Employee = _
  var employeeJson: JsValue = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    employee = Employee(42, "Foo", "Bar", "New York", "United States", Seq("engineering", "new", "bar"))
  }

  @TearDown(Level.Iteration)
  def tearDown(): Unit = {
    assert(employeeJson == Json.parse(""" {
        |   "employeeNumber": 42,
        |   "firstName": "Foo",
        |   "lastName": "Bar",
        |   "city": "New York",
        |   "country": "United States",
        |   "tags": ["engineering", "new", "bar"]
        | }
      """.stripMargin))
  }

  @Benchmark
  def toJsonPlayMacro(): JsValue = {
    employeeJson = Json.toJson(employee)
    employeeJson
  }

  @Benchmark
  def toJsonMagnoliaMacro(): JsValue = {
    employeeJson = Json.toJson(employee)(Employee.magnoliaWrites)
    employeeJson
  }

}
