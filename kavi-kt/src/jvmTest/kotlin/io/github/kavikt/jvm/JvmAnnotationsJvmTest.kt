/*
 * Copyright (C) 2018 Square, Inc.
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
package io.github.kavikt.jvm

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.kavikt.ClassName
import io.github.kavikt.FileSpec
import io.github.kavikt.FunSpec
import io.github.kavikt.PropertySpec
import io.github.kavikt.TypeSpec
import io.github.kavikt.addParameter
import io.github.kavikt.builder
import java.io.IOException
import kotlin.test.Test

class JvmAnnotationsJvmTest {

  @Test
  fun throwsFunction() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addFunction(
          FunSpec.builder("foo").throws(IOException::class, IllegalArgumentException::class).build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.IOException
        |import java.lang.IllegalArgumentException
        |import kotlin.jvm.Throws
        |
        |@Throws(
        |  IOException::class,
        |  IllegalArgumentException::class,
        |)
        |public fun foo() {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun throwsFunctionCustomException() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addFunction(
          FunSpec.builder("foo")
            .throws(ClassName("com.squareup.tacos", "IllegalTacoException"))
            .build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.jvm.Throws
        |
        |@Throws(IllegalTacoException::class)
        |public fun foo() {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun throwsPrimaryConstructor() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .primaryConstructor(
              FunSpec.constructorBuilder()
                .throws(IOException::class)
                .addParameter("foo", String::class)
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
        |import java.io.IOException
        |import kotlin.String
        |import kotlin.jvm.Throws
        |
        |public class Taco @Throws(IOException::class) constructor(
        |  foo: String,
        |)
        |"""
          .trimMargin()
      )
  }

  @Test
  fun throwsGetter() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addProperty(
          PropertySpec.builder("foo", String::class)
            .getter(
              FunSpec.getterBuilder()
                .throws(IOException::class)
                .addStatement("return %S", "foo")
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
        |import java.io.IOException
        |import kotlin.String
        |import kotlin.jvm.Throws
        |
        |public val foo: String
        |  @Throws(IOException::class)
        |  get() = "foo"
        |"""
          .trimMargin()
      )
  }

  @Test
  fun throwsSetter() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addProperty(
          PropertySpec.builder("foo", String::class)
            .mutable()
            .setter(
              FunSpec.setterBuilder()
                .throws(IOException::class)
                .addParameter("value", String::class)
                .addStatement("print(%S)", "foo")
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
        |import java.io.IOException
        |import kotlin.String
        |import kotlin.jvm.Throws
        |
        |public var foo: String
        |  @Throws(IOException::class)
        |  set(`value`) {
        |    print("foo")
        |  }
        |"""
          .trimMargin()
      )
  }
}
