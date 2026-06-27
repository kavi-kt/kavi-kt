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

// Reflection is the subject here (String::class.asTypeName() / passing a raw KClass to %T must
// resolve to kotlin.String), and reflection throws on js/wasm.
@OptIn(ExperimentalKaviApi::class)
class TypeSpecTestJvmNative {

  @Test
  fun typeFromTypeName() {
    val typeName = String::class.asTypeName()
    assertThat(CodeBlock.of("%T", typeName).toString()).isEqualTo("kotlin.String")
  }

  @Test
  fun typeFromReflectType() {
    assertThat(CodeBlock.of("%T", String::class).toString()).isEqualTo("kotlin.String")
  }
}
