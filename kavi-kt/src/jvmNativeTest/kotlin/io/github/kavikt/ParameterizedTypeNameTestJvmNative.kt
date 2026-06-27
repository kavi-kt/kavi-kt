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
package io.github.kavikt

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import kotlin.test.Test

/**
 * These exercise reflection (KClass.plusParameter / KClass.asTypeName, plus nested-class
 * reflection), which relies on KClass.qualifiedName and is stripped on Kotlin/JS|Wasm.
 */
class ParameterizedTypeNameTestJvmNative {
  @Test
  fun kClassPlusParameter() {
    val typeName = List::class.plusParameter(String::class)
    assertThat(typeName.toString()).isEqualTo("kotlin.collections.List<kotlin.String>")
  }

  @Test
  fun kClassPlusTwoParameters() {
    val typeName = Map::class.plusParameter(String::class).plusParameter(Int::class)
    assertThat(typeName.toString()).isEqualTo("kotlin.collections.Map<kotlin.String, kotlin.Int>")
  }

  @Test
  fun primitiveArray() {
    assertThat(ByteArray::class.asTypeName().toString()).isEqualTo("kotlin.ByteArray")
    assertThat(CharArray::class.asTypeName().toString()).isEqualTo("kotlin.CharArray")
    assertThat(ShortArray::class.asTypeName().toString()).isEqualTo("kotlin.ShortArray")
    assertThat(IntArray::class.asTypeName().toString()).isEqualTo("kotlin.IntArray")
    assertThat(LongArray::class.asTypeName().toString()).isEqualTo("kotlin.LongArray")
    assertThat(FloatArray::class.asTypeName().toString()).isEqualTo("kotlin.FloatArray")
    assertThat(DoubleArray::class.asTypeName().toString()).isEqualTo("kotlin.DoubleArray")
  }

  private class Enclosing1 {
    class GenericClass<T>
  }

  private object Enclosing2 {
    class Foo
  }

  @Test
  fun equalsAndHashCode() {
    val parameterizedTypeName1 =
      Enclosing1.GenericClass::class.parameterizedBy(Enclosing2.Foo::class)
    val parameterizedTypeName2 =
      Enclosing1.GenericClass::class.parameterizedBy(Enclosing2.Foo::class)
    assertThat(parameterizedTypeName1).isEqualTo(parameterizedTypeName2)
    assertThat(parameterizedTypeName1.hashCode()).isEqualTo(parameterizedTypeName2.hashCode())

    assertThat(parameterizedTypeName1.copy(nullable = true)).isNotEqualTo(parameterizedTypeName1)

    assertThat(
        parameterizedTypeName1.copy(
          annotations = listOf(AnnotationSpec.builder(Suppress::class).build())
        )
      )
      .isNotEqualTo(parameterizedTypeName1)
  }

  @Test
  fun equalsAndHashCodeIgnoreTags() {
    val parameterizedTypeName =
      Enclosing1.GenericClass::class.parameterizedBy(Enclosing2.Foo::class)
    val tagged = parameterizedTypeName.copy(tags = mapOf(String::class to "test"))

    assertThat(parameterizedTypeName).isEqualTo(tagged)
    assertThat(parameterizedTypeName.hashCode()).isEqualTo(tagged.hashCode())
  }
}
