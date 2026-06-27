/*
 * Copyright (C) 2017 Square, Inc.
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
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import io.github.kavikt.TypeVariableName.Companion.NULLABLE_ANY_LIST
import kotlin.test.Test

class TypeVariableNameTest {
  @Test
  fun nullableAnyIsImplicitBound() {
    val typeVariableName = TypeVariableName("T")
    assertThat(typeVariableName.bounds).containsExactly(NULLABLE_ANY)
  }

  @Test
  fun oneTypeVariableNoBounds() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T"))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public fun <T> foo(): T? = null
        |"""
          .trimMargin()
      )
  }

  @Test
  fun twoTypeVariablesNoBounds() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T"))
        .addTypeVariable(TypeVariableName("U"))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public fun <T, U> foo(): T? = null
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: oneTypeVariableOneBound

  // kmp: this test was extracted: twoTypeVariablesOneBoundEach

  // kmp: this test was extracted: oneTypeVariableTwoBounds

  // kmp: this test was extracted: twoTypeVariablesTwoBoundsEach

  // kmp: this test was extracted: threeTypeVariables

  @Test
  fun addingBoundsRemovesImplicitBound() {
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addTypeVariable(TypeVariableName("T").copy(bounds = listOf(NUMBER)))
        .build()
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<T : kotlin.Number>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun inVariance() {
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addTypeVariable(TypeVariableName("E", NUMBER, variance = KModifier.IN))
        .build()
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<in E : kotlin.Number>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun outVariance() {
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addTypeVariable(TypeVariableName("E", NUMBER, variance = KModifier.OUT))
        .build()
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<out E : kotlin.Number>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun invalidVariance() {
    assertFailure { TypeVariableName("E", KModifier.FINAL) }
      .isInstanceOf<IllegalArgumentException>()
  }

  @Test
  fun reified() {
    val funSpec =
      FunSpec.builder("printMembers")
        .addModifiers(KModifier.INLINE)
        .addTypeVariable(TypeVariableName("T").copy(reified = true))
        .addStatement("println(T::class.members)")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public inline fun <reified T> printMembers() {
        |  println(T::class.members)
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun anyBoundsIsLegal() {
    val typeSpec = TypeSpec.classBuilder("Taco").addTypeVariable(TypeVariableName("E", ANY)).build()
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<E : kotlin.Any>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun filterOutNullableAnyBounds() {
    val typeSpec =
      TypeSpec.classBuilder("Taco").addTypeVariable(TypeVariableName("E", NULLABLE_ANY)).build()
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<E>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun emptyBoundsShouldDefaultToAnyNullable() {
    val typeVariable = TypeVariableName("E", bounds = emptyArray<TypeName>())
    val typeSpec = TypeSpec.classBuilder("Taco").addTypeVariable(typeVariable).build()
    assertThat(typeVariable.bounds).isEqualTo(NULLABLE_ANY_LIST)
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<E>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun noBoundsShouldDefaultToAnyNullable() {
    val typeVariable = TypeVariableName("E")
    val typeSpec = TypeSpec.classBuilder("Taco").addTypeVariable(typeVariable).build()
    assertThat(typeVariable.bounds).isEqualTo(NULLABLE_ANY_LIST)
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<E>
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: genericClassNoBoundsShouldDefaultToAnyNullable

  @Test
  fun equalsAndHashCode() {
    val typeVariableName1 = TypeVariableName("E", listOf(NUMBER), KModifier.IN)

    val typeVariableName2 = TypeVariableName("E", listOf(NUMBER), KModifier.IN)
    assertThat(typeVariableName1).isEqualTo(typeVariableName2)
    assertThat(typeVariableName1.hashCode()).isEqualTo(typeVariableName2.hashCode())
    assertThat(typeVariableName1.toString()).isEqualTo(typeVariableName2.toString())

    assertThat(typeVariableName1.copy(nullable = true)).isNotEqualTo(typeVariableName1)

    assertThat(
        typeVariableName1.copy(
          annotations = listOf(AnnotationSpec.builder(ClassName("kotlin", "Suppress")).build())
        )
      )
      .isNotEqualTo(typeVariableName1)

    assertThat(typeVariableName1.copy(bounds = listOf(STRING))).isNotEqualTo(typeVariableName1)

    assertThat(typeVariableName1.copy(reified = true)).isNotEqualTo(typeVariableName1)
  }

  @Test
  fun equalsAndHashCodeIgnoreTags() {
    val typeVariableName = TypeVariableName("E", listOf(NUMBER), KModifier.IN)
    val tagged = typeVariableName.copy(tags = mapOf(String::class to "test"))

    assertThat(typeVariableName).isEqualTo(tagged)
    assertThat(typeVariableName.hashCode()).isEqualTo(tagged.hashCode())
  }

  // kmp: this test was extracted: recursivelyBoundedTypeVariableEqualsAndHashCode

  @Test
  fun equalsComparesNestedBounds() {
    fun boundedBy(argument: TypeName) =
      TypeVariableName("T")
        .copy(bounds = listOf(LIST.parameterizedBy(WildcardTypeName.producerOf(argument))))

    assertThat(boundedBy(NUMBER)).isEqualTo(boundedBy(NUMBER))
    assertThat(boundedBy(NUMBER)).isNotEqualTo(boundedBy(STRING))
  }

  @Test
  fun equalsComparesEnclosingTypeOfBound() {
    fun boundedByInnerOf(outerArgument: TypeName) =
      TypeVariableName("T")
        .copy(
          bounds =
            listOf(
              ClassName("com.example", "Outer")
                .parameterizedBy(outerArgument)
                .nestedClass("Inner", listOf(STRING))
            )
        )

    assertThat(boundedByInnerOf(INT)).isEqualTo(boundedByInnerOf(INT))
    assertThat(boundedByInnerOf(INT)).isNotEqualTo(boundedByInnerOf(STRING))
  }
}
