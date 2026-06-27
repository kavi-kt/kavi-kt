/*
 * Copyright (C) 2019 Square, Inc.
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
import org.junit.Before
import org.junit.Test

class MemberNameJvmTest {
  @Test
  fun keywordsEscaping() {
    val `when` = MemberName("org.mockito", "when")
    val file =
      FileSpec.builder("com.squareup.tacos", "TacoTest")
        .addType(
          TypeSpec.classBuilder("TacoTest")
            .addFunction(
              FunSpec.builder("setUp")
                .addAnnotation(Before::class)
                .addStatement("%M(tacoService.createTaco()).thenReturn(tastyTaco())", `when`)
                .build()
            )
            .build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import org.junit.Before
        |import org.mockito.`when`
        |
        |public class TacoTest {
        |  @Before
        |  public fun setUp() {
        |    `when`(tacoService.createTaco()).thenReturn(tastyTaco())
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun memberExtension_class() {
    assertThat(Regex::class.java.member("fromLiteral"))
      .isEqualTo(MemberName(ClassName("kotlin.text", "Regex"), "fromLiteral"))
  }
}
