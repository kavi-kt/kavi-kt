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

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.reflect.typeOf
import kotlin.test.Test

// Kotlin/Native reflection can't recover a nested generic's arity, so these APIs reject it.
class TypeNameNativeTest {

  @Test
  fun typeNameOf_nestedGenericClass_throws() {
    val failure = runCatching { typeNameOf<Map.Entry<String, Int>>() }.exceptionOrNull()
    assertThat(failure).isNotNull()
    assertThat(failure!!.message).isNotNull()
    assertThat(failure.message!!).contains("kotlin.collections.Map.Entry")
    assertThat(failure.message!!).contains("parameterizedBy")
  }

  // asTypeNameOrNull returns null on Kotlin/Native instead of throwing.
  @Test
  fun asTypeNameOrNull_nestedGenericClass_isNull() {
    assertThat(typeOf<Map.Entry<String, Int>>().asTypeNameOrNull()).isNull()
  }
}
