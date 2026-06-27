/*
 * Copyright (C) 2017 Square, Inc.
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
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import io.github.kavikt.jvm.jvmField
import io.github.kavikt.jvm.jvmSuppressWildcards
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class KaviJvmTest {
  private val tacosPackage = "com.squareup.tacos"

  @Test
  fun enumAsDefaultArgument() {
    val source =
      FileSpec.builder(tacosPackage, "Taco")
        .addFunction(
          FunSpec.builder("timeout")
            .addParameter("duration", Long::class)
            .addParameter(
              ParameterSpec.builder("timeUnit", TimeUnit::class)
                .defaultValue("%T.%L", TimeUnit::class, TimeUnit.MILLISECONDS.name)
                .build()
            )
            .addStatement("this.timeout = timeUnit.toMillis(duration)")
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.concurrent.TimeUnit
        |import kotlin.Long
        |
        |public fun timeout(duration: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) {
        |  this.timeout = timeUnit.toMillis(duration)
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun primaryConstructorParameterAnnotation() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .primaryConstructor(
              FunSpec.constructorBuilder().addParameter("foo", String::class).build()
            )
            .addProperty(
              PropertySpec.builder("foo", String::class).jvmField().initializer("foo").build()
            )
            .build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.String
        |import kotlin.jvm.JvmField
        |
        |public class Taco(
        |  @JvmField
        |  public val foo: String,
        |)
        |"""
          .trimMargin()
      )
  }

  // https://github.com/square/kotlinpoet/issues/346
  @Test
  fun importTypeArgumentInParameterizedTypeName() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addFunction(
          FunSpec.builder("foo")
            .addParameter(
              "a",
              List::class.asTypeName()
                .parameterizedBy(Int::class.asTypeName().jvmSuppressWildcards()),
            )
            .build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.Int
        |import kotlin.collections.List
        |import kotlin.jvm.JvmSuppressWildcards
        |
        |public fun foo(a: List<@JvmSuppressWildcards Int>) {
        |}
        |"""
          .trimMargin()
      )
  }
}
