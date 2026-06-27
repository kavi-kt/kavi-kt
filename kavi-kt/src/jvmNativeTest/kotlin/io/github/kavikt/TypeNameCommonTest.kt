/*
 * Copyright (C) 2021 Square, Inc.
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
import kotlin.reflect.typeOf
import kotlin.test.Test

/**
 * KType -> TypeName reconstruction is the subject here; for non-nested generics it works on jvm +
 * native because the stdlib KType API is populated there, but not on js/wasm.
 */
class TypeNameCommonTest {

  @Test
  fun typeNameOf_simple() {
    val type = typeNameOf<TypeNameCommonTest>()
    assertThat(type.toString()).isEqualTo("io.github.kavikt.TypeNameCommonTest")
  }

  @Test
  fun typeNameOf_simple_intrinsic() {
    val type = typeNameOf<String>()
    assertThat(type.toString()).isEqualTo("kotlin.String")
  }

  @Test
  fun typeNameOf_array_primitive() {
    val type = typeNameOf<IntArray>()
    assertThat(type.toString()).isEqualTo("kotlin.IntArray")
  }

  @Test
  fun typeNameOf_array_parameterized() {
    val type = typeNameOf<Array<String>>()
    assertThat(type.toString()).isEqualTo("kotlin.Array<kotlin.String>")
  }

  @Test
  fun typeNameOf_nullable() {
    val type = typeNameOf<String?>()
    assertThat(type.toString()).isEqualTo("kotlin.String?")
  }

  @Test
  fun typeNameOf_generic() {
    val type = typeNameOf<List<String>>()
    assertThat(type.toString()).isEqualTo("kotlin.collections.List<kotlin.String>")
  }

  // A non-nested generic reconstructs, so asTypeNameOrNull matches asTypeName (non-null).
  @Test
  fun asTypeNameOrNull_nonNestedGeneric_matchesAsTypeName() {
    val type = typeOf<Map<String, Int>>()
    assertThat(type.asTypeNameOrNull()).isEqualTo(type.asTypeName())
    assertThat(type.asTypeNameOrNull().toString())
      .isEqualTo("kotlin.collections.Map<kotlin.String, kotlin.Int>")
  }
}
