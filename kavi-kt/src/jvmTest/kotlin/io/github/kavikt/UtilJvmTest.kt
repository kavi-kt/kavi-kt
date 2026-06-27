/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.kavikt

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class UtilJvmTest {
  @Test
  fun escapeSpaceInName() {
    val generated =
      FileSpec.builder("a", "b")
        .addFunction(
          FunSpec.builder("foo")
            .apply {
              addParameter("aaa bbb", typeNameOf<(Int) -> String>())
              val arg = mutableListOf<String>()
              addStatement(
                StringBuilder()
                  .apply {
                    repeat(10) {
                      append("%N($it) +♢")
                      arg += "aaa bbb"
                    }
                    append("%N(100)")
                    arg += "aaa bbb"
                  }
                  .toString(),
                *arg.toTypedArray(),
              )
            }
            .build()
        )
        .build()
        .toString()

    val expectedOutput =
      """
      package a

      import kotlin.Function1
      import kotlin.Int
      import kotlin.String

      public fun foo(`aaa bbb`: Function1<Int, String>) {
        `aaa bbb`(0) + `aaa bbb`(1) + `aaa bbb`(2) + `aaa bbb`(3) + `aaa bbb`(4) + `aaa bbb`(5) +
            `aaa bbb`(6) + `aaa bbb`(7) + `aaa bbb`(8) + `aaa bbb`(9) + `aaa bbb`(100)
      }

      """
        .trimIndent()

    assertThat(generated).isEqualTo(expectedOutput)
  }
}
