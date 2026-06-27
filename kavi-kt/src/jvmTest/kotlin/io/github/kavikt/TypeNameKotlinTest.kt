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
import assertk.assertions.isNotNull
import kotlin.reflect.typeOf
import org.junit.Test

class TypeNameKotlinTest {

  // kmp: this test was extracted: typeNameOf_simple

  // kmp: this test was extracted: typeNameOf_simple_intrinsic

  // kmp: this test was extracted: typeNameOf_array_primitive

  // kmp: this test was extracted: typeNameOf_array_parameterized

  // kmp: this test was extracted: typeNameOf_nullable

  // kmp: this test was extracted: typeNameOf_generic

  @Test
  fun typeNameOf_generic_wildcard_out() {
    val type = typeNameOf<GenericType<out String>>()
    assertThat(type.toString())
      .isEqualTo("io.github.kavikt.TypeNameKotlinTest.GenericType<out kotlin.String>")
  }

  @Test
  fun typeNameOf_generic_wildcard_in() {
    val type = typeNameOf<GenericType<in String>>()
    assertThat(type.toString())
      .isEqualTo("io.github.kavikt.TypeNameKotlinTest.GenericType<in kotlin.String>")
  }

  @Test
  fun typeNameOf_complex() {
    val type =
      typeNameOf<Map<String, List<Map<*, GenericType<in Set<Array<GenericType<out String>?>>>>>>>()
    assertThat(type.toString())
      .isEqualTo(
        "kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.collections.Map<*, io.github.kavikt.TypeNameKotlinTest.GenericType<in kotlin.collections.Set<kotlin.Array<io.github.kavikt.TypeNameKotlinTest.GenericType<out kotlin.String>?>>>>>>"
      )
  }

  // A nested generic class resolves via kotlin-reflect, so asTypeNameOrNull is non-null.
  @Test
  fun asTypeNameOrNull_nestedGenericClass_isNotNull() {
    val type = typeOf<Map.Entry<String, Int>>()
    assertThat(type.asTypeNameOrNull()).isNotNull()
    assertThat(type.asTypeNameOrNull()).isEqualTo(type.asTypeName())
  }

  @Suppress("unused") class GenericType<T>

  @Test
  fun tag() {
    val type = typeNameOf<String>().copy(tags = mapOf(String::class to "Test"))
    assertThat(type.tag<String>()).isEqualTo("Test")
  }

  @Test
  fun existingTagsShouldBePreserved() {
    val type = typeNameOf<String>().copy(tags = mapOf(String::class to "Test"))
    val copied = type.copy(nullable = true)
    assertThat(copied.tag<String>()).isEqualTo("Test")
  }

  @Test
  fun annotated_withAnnotationSpec() {
    val annotation = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
    val type = typeNameOf<String>().annotated(annotation)
    assertThat(type.annotations).isEqualTo(listOf(annotation))
    assertThat(type.toString()).isEqualTo("@kotlin.Suppress(\"unused\") kotlin.String")
  }

  @Test
  fun annotated_withMultipleAnnotationSpecs() {
    val suppress = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
    val deprecated = AnnotationSpec.builder(Deprecated::class).addMember("%S", "test").build()
    val type = typeNameOf<String>().annotated(suppress, deprecated)
    assertThat(type.annotations).isEqualTo(listOf(suppress, deprecated))
  }

  @Test
  fun annotated_withAnnotationList() {
    val annotation = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
    val type = typeNameOf<String>().annotated(listOf(annotation))
    assertThat(type.annotations).isEqualTo(listOf(annotation))
  }

  @Test
  fun annotated_withKClass() {
    val type = typeNameOf<String>().annotated(Suppress::class)
    assertThat(type.annotations.size).isEqualTo(1)
    assertThat(type.annotations[0].typeName).isEqualTo(Suppress::class.asClassName())
  }

  @Test
  fun annotated_withMultipleKClasses() {
    val type = typeNameOf<String>().annotated(Suppress::class, Deprecated::class)
    assertThat(type.annotations.size).isEqualTo(2)
    assertThat(type.annotations[0].typeName).isEqualTo(Suppress::class.asClassName())
    assertThat(type.annotations[1].typeName).isEqualTo(Deprecated::class.asClassName())
  }

  @Test
  fun annotated_withClassName() {
    val suppressClassName = Suppress::class.asClassName()
    val type = typeNameOf<String>().annotated(suppressClassName)
    assertThat(type.annotations.size).isEqualTo(1)
    assertThat(type.annotations[0].typeName).isEqualTo(suppressClassName)
  }

  @Test
  fun annotated_chainingMultipleCalls() {
    val suppress = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
    val deprecated = AnnotationSpec.builder(Deprecated::class).addMember("%S", "test").build()
    val type = typeNameOf<String>().annotated(suppress).annotated(deprecated)
    assertThat(type.annotations).isEqualTo(listOf(suppress, deprecated))
  }

  @Test
  fun annotated_preservesExistingAnnotations() {
    val suppress = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
    val deprecated = AnnotationSpec.builder(Deprecated::class).addMember("%S", "test").build()
    val typeWithSuppressAnnotation = typeNameOf<String>().copy(annotations = listOf(suppress))
    val typeWithBothAnnotations = typeWithSuppressAnnotation.annotated(deprecated)
    assertThat(typeWithBothAnnotations.annotations).isEqualTo(listOf(suppress, deprecated))
  }
}
