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
import io.github.kavikt.ParameterizedTypeName.Companion.plusParameter
import kotlin.test.Test

class ParameterizedTypeNameTest {
  @Test
  fun classNamePlusParameter() {
    val typeName =
      ClassName("kotlin.collections", "List").plusParameter(ClassName("kotlin", "String"))
    assertThat(typeName.toString()).isEqualTo("kotlin.collections.List<kotlin.String>")
  }

  @Test
  fun classNamePlusTwoParameters() {
    val typeName =
      ClassName("kotlin.collections", "Map")
        .plusParameter(ClassName("kotlin", "String"))
        .plusParameter(ClassName("kotlin", "Int"))
    assertThat(typeName.toString()).isEqualTo("kotlin.collections.Map<kotlin.String, kotlin.Int>")
  }

  @Test
  fun classNamePlusTypeVariableParameter() {
    val t = TypeVariableName("T")
    val mapOfT = MAP.plusParameter(t)
    assertThat(mapOfT.toString()).isEqualTo("kotlin.collections.Map<T>")
  }

  // kmp: this test was extracted: kClassPlusParameter

  // kmp: this test was extracted: kClassPlusTwoParameters

  // kmp: this test was extracted: classPlusParameter

  // kmp: this test was extracted: primitiveArray

  // kmp: this test was extracted: arrayPlusPrimitiveParameter

  // kmp: this test was extracted: arrayPlusObjectParameter

  // kmp: this test was extracted: arrayPlusNullableParameter

  // kmp: this test was extracted: typeParameter

  // kmp: this test was extracted: nullableTypeParameter

  // kmp: this test was extracted: classPlusTwoParameters

  // kmp: this test was extracted: copyingTypeArguments

  // kmp: this test was extracted: kTypeInvariantNullableProjection

  // kmp: this test was extracted: kTypeMultiVariantProjection

  // kmp: this test was extracted: kTypeOutAnyOnTypeWithoutBoundsVariance

  @Test
  fun annotatedLambdaTypeParameter() {
    val annotation = AnnotationSpec.builder(ClassName("", "Annotation")).build()
    val typeName =
      MAP.plusParameter(STRING)
        .plusParameter(LambdaTypeName.get(returnType = UNIT).copy(annotations = listOf(annotation)))
    assertThat(typeName.toString())
      .isEqualTo("kotlin.collections.Map<kotlin.String, @Annotation () -> kotlin.Unit>")
  }

  // kmp: this test was extracted: equalsAndHashCode

  // kmp: this test was extracted: equalsAndHashCodeIgnoreTags

  // kmp: this test was extracted: stackOverflowOnRecursivelyBoundGeneric

}
