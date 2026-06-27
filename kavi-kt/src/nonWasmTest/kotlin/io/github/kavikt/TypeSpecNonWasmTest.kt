/*
 * Copyright (C) 2025 Square, Inc.
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

import assertk.assertFailure
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import io.github.kavikt.KModifier.ABSTRACT
import io.github.kavikt.KModifier.INTERNAL
import io.github.kavikt.KModifier.PRIVATE
import kotlin.test.Test

// The Kotlin/Wasm backend throws ClassCastException instead of IllegalArgumentException from
// the interface-member modifier check, so these negative tests run on every target except wasmJs.
class TypeSpecNonWasmTest {

  @Test
  fun internalFunForbiddenInInterface() {
    val type = TypeSpec.interfaceBuilder("ITaco")

    assertFailure {
        type.addFunction(FunSpec.builder("eat").addModifiers(ABSTRACT, INTERNAL).build()).build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("modifiers [ABSTRACT, INTERNAL] must contain none of [INTERNAL, PROTECTED]")

    assertFailure {
        type
          .addFunctions(listOf(FunSpec.builder("eat").addModifiers(ABSTRACT, INTERNAL).build()))
          .build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("modifiers [ABSTRACT, INTERNAL] must contain none of [INTERNAL, PROTECTED]")
  }

  @Test
  fun privateAbstractFunForbiddenInInterface() {
    val type = TypeSpec.interfaceBuilder("ITaco")

    assertFailure {
        type.addFunction(FunSpec.builder("eat").addModifiers(ABSTRACT, PRIVATE).build()).build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "modifiers [ABSTRACT, PRIVATE] must contain none or only one of [ABSTRACT, PRIVATE]"
      )

    assertFailure {
        type
          .addFunctions(listOf(FunSpec.builder("eat").addModifiers(ABSTRACT, PRIVATE).build()))
          .build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "modifiers [ABSTRACT, PRIVATE] must contain none or only one of [ABSTRACT, PRIVATE]"
      )
  }
}
