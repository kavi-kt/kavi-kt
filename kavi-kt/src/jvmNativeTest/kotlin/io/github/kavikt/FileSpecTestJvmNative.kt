/*
 * Copyright (C) 2015 Square, Inc.
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

// Resolves an enum constant's declaring ClassName through its runtime KClass, which works on jvm +
// native but not js/wasm (they strip KClass.qualifiedName from `::class` literals).
class FileSpecTestJvmNative {

  // addImport(Enum<*>) resolves the constant's declaring enum via its runtime KClass.
  @Test
  fun importEnumConstant() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addProperty(
          PropertySpec.builder("filling", FileSpecTestFilling::class.asClassName())
            .initializer("%L", FileSpecTestFilling.SHELL.name)
            .build()
        )
        .addImport(FileSpecTestFilling.SHELL)
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.FileSpecTestFilling
        |import io.github.kavikt.FileSpecTestFilling.SHELL
        |
        |public val filling: FileSpecTestFilling = SHELL
        |"""
          .trimMargin()
      )
  }
}

internal enum class FileSpecTestFilling {
  SHELL,
  BEANS,
}
