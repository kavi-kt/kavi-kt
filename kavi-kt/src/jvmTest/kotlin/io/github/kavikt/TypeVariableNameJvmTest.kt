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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import io.github.kavikt.TypeVariableName.Companion.NULLABLE_ANY_LIST
import java.io.Serializable
import kotlin.test.Test

class TypeVariableNameJvmTest {
  @Test
  fun oneTypeVariableOneBound() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T", Serializable::class))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public fun <T : java.io.Serializable> foo(): T? = null
        |"""
          .trimMargin()
      )
  }

  @Test
  fun twoTypeVariablesOneBoundEach() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T", Serializable::class))
        .addTypeVariable(TypeVariableName("U", Runnable::class))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public fun <T : java.io.Serializable, U : java.lang.Runnable> foo(): T? = null
        |"""
          .trimMargin()
      )
  }

  @Test
  fun oneTypeVariableTwoBounds() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T", Serializable::class, Runnable::class))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public fun <T> foo(): T? where T : java.io.Serializable, T : java.lang.Runnable = null
        |"""
          .trimMargin()
      )
  }

  @Test
  fun twoTypeVariablesTwoBoundsEach() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T", Serializable::class, Runnable::class))
        .addTypeVariable(TypeVariableName("U", Comparator::class, Cloneable::class))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        "public fun <T, U> foo(): " +
          "T? where T : java.io.Serializable, T : java.lang.Runnable, " +
          "U : java.util.Comparator, U : kotlin.Cloneable = null\n"
      )
  }

  @Test
  fun threeTypeVariables() {
    val funSpec =
      FunSpec.builder("foo")
        .addTypeVariable(TypeVariableName("T", Serializable::class, Runnable::class))
        .addTypeVariable(TypeVariableName("U", Cloneable::class))
        .addTypeVariable(TypeVariableName("V"))
        .returns(TypeVariableName("T").copy(nullable = true))
        .addStatement("return null")
        .build()
    assertThat(funSpec.toString())
      .isEqualTo(
        "public fun <T, U : kotlin.Cloneable, V> foo(): " +
          "T? where T : java.io.Serializable, T : java.lang.Runnable = null\n"
      )
  }

  @Test
  fun genericClassNoBoundsShouldDefaultToAnyNullable() {
    val typeVariable = TypeVariableName.get(GenericClass::class.java.typeParameters[0])
    val typeSpec = TypeSpec.classBuilder("Taco").addTypeVariable(typeVariable).build()
    assertThat(typeVariable.bounds).isEqualTo(NULLABLE_ANY_LIST)
    assertThat(typeSpec.toString())
      .isEqualTo(
        """
        |public class Taco<T>
        |"""
          .trimMargin()
      )
  }

  class GenericClass<T>

  @Test
  fun equalsAndHashCodeWithJavaBound() {
    val typeVariableName1 = TypeVariableName("E", listOf(Number::class.asTypeName()), KModifier.IN)
    assertThat(typeVariableName1.copy(bounds = listOf(Runnable::class.asTypeName())))
      .isNotEqualTo(typeVariableName1)
  }

  @Test
  fun recursivelyBoundedTypeVariableEqualsAndHashCode() {
    // `Enum<E : Enum<E>>` produces a type variable whose bounds refer back to itself.
    val typeParameter = Enum::class.java.typeParameters[0]
    val first = TypeVariableName.get(typeParameter)
    val second = TypeVariableName.get(typeParameter)

    assertThat(first.hashCode()).isEqualTo(second.hashCode())
    assertThat(first).isEqualTo(second)
    assertThat(second).isEqualTo(first)
    assertThat(first).isNotEqualTo(TypeVariableName("E"))
  }
}
