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

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class AnnotatedTypeNameJvmTest {
  private val NEVER_NULL = AnnotationSpec.builder(NeverNull::class).build()
  private val NN = NeverNull::class.java.canonicalName

  annotation class NeverNull

  @Test
  fun annotatedTwice() {
    val expected = "@$NN @java.lang.Override kotlin.String"
    val type = String::class.asTypeName()
    val actual =
      type
        .copy(
          annotations =
            type.annotations + NEVER_NULL + AnnotationSpec.builder(Override::class).build()
        )
        .toString()
    assertEquals(expected, actual)
  }

  @Test
  fun annotatedEquivalenceWithJavaObject() {
    annotatedEquivalence(WildcardTypeName.producerOf(Object::class))
  }

  private fun annotatedEquivalence(type: TypeName) {
    assertFalse(type.isAnnotated)
    assertEquals(type, type)
    assertEquals(
      type.copy(annotations = listOf(NEVER_NULL)),
      type.copy(annotations = listOf(NEVER_NULL)),
    )
    assertNotEquals(type, type.copy(annotations = listOf(NEVER_NULL)))
    assertEquals(type.hashCode().toLong(), type.hashCode().toLong())
    assertEquals(
      type.copy(annotations = listOf(NEVER_NULL)).hashCode().toLong(),
      type.copy(annotations = listOf(NEVER_NULL)).hashCode().toLong(),
    )
    assertNotEquals(
      type.hashCode().toLong(),
      type.copy(annotations = listOf(NEVER_NULL)).hashCode().toLong(),
    )
  }

  // https://github.com/square/javapoet/issues/431
  // @Target(ElementType.TYPE_USE) requires Java 1.8
  annotation class TypeUseAnnotation

  // https://github.com/square/javapoet/issues/431
  @Ignore
  @Test
  fun annotatedNestedType() {
    val expected =
      "kotlin.collections.Map.@" + TypeUseAnnotation::class.java.canonicalName + " Entry"
    val typeUseAnnotation = AnnotationSpec.builder(TypeUseAnnotation::class).build()
    val type = Map.Entry::class.asTypeName().copy(annotations = listOf(typeUseAnnotation))
    val actual = type.toString()
    assertEquals(expected, actual)
  }

  // https://github.com/square/javapoet/issues/431
  @Ignore
  @Test
  fun annotatedNestedParameterizedType() {
    val expected =
      "kotlin.collections.Map.@" +
        TypeUseAnnotation::class.java.canonicalName +
        " Entry<kotlin.Byte, kotlin.Byte>"
    val typeUseAnnotation = AnnotationSpec.builder(TypeUseAnnotation::class).build()
    val type =
      Map.Entry::class.parameterizedBy(Byte::class, Byte::class)
        .copy(annotations = listOf(typeUseAnnotation))
    val actual = type.toString()
    assertEquals(expected, actual)
  }
}
