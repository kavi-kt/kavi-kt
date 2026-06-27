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

import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AnnotatedTypeNameTest {
  private val NEVER_NULL_NAME = ClassName("io.github.kavikt", "AnnotatedTypeNameTest", "NeverNull")
  private val NEVER_NULL = AnnotationSpec.builder(NEVER_NULL_NAME).build()
  private val NN = NEVER_NULL_NAME.canonicalName

  @Test
  fun annotated() {
    val simpleString = STRING
    assertFalse(simpleString.isAnnotated)
    assertEquals(simpleString, STRING)
    val annotated = simpleString.copy(annotations = simpleString.annotations + NEVER_NULL)
    assertTrue(annotated.isAnnotated)
  }

  @Test
  fun annotatedType() {
    val expected = "@$NN kotlin.String"
    val type = STRING
    val actual = type.copy(annotations = type.annotations + NEVER_NULL).toString()
    assertEquals(expected, actual)
  }

  // kmp: this test was extracted: annotatedTwice

  @Test
  fun annotatedParameterizedType() {
    val expected = "@$NN kotlin.collections.List<kotlin.String>"
    val type = LIST.parameterizedBy(STRING)
    val actual = type.copy(annotations = type.annotations + NEVER_NULL).toString()
    assertEquals(expected, actual)
  }

  @Test
  fun annotatedArgumentOfParameterizedType() {
    val expected = "kotlin.collections.List<@$NN kotlin.String>"
    val type = STRING.copy(annotations = listOf(NEVER_NULL))
    val list = LIST
    val actual = list.parameterizedBy(type).toString()
    assertEquals(expected, actual)
  }

  @Test
  fun annotatedWildcardTypeNameWithSuper() {
    val expected = "in @$NN kotlin.String"
    val type = STRING.copy(annotations = listOf(NEVER_NULL))
    val actual = WildcardTypeName.consumerOf(type).toString()
    assertEquals(expected, actual)
  }

  @Test
  fun annotatedWildcardTypeNameWithExtends() {
    val expected = "out @$NN kotlin.String"
    val type = STRING.copy(annotations = listOf(NEVER_NULL))
    val actual = WildcardTypeName.producerOf(type).toString()
    assertEquals(expected, actual)
  }

  @Test
  fun annotatedEquivalence() {
    annotatedEquivalence(UNIT)
    annotatedEquivalence(ANY)
    annotatedEquivalence(LIST.parameterizedBy(ANY))
    annotatedEquivalence(TypeVariableName("A"))
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

  // kmp: this test was extracted: annotatedNestedType

  // kmp: this test was extracted: annotatedNestedParameterizedType

}
