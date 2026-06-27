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
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.kavikt.KModifier.PRIVATE
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import java.io.Serializable
import java.util.function.Function
import kotlin.test.Test

@OptIn(ExperimentalKaviApi::class)
class PropertySpecJvmTest {
  annotation class TestAnnotation

  @Test
  fun generalBuilderEqualityTest() {
    val originatingElement = FakeElement()
    val prop =
      PropertySpec.builder("tacos", Int::class)
        .mutable()
        .addAnnotation(ClassName("io.github.kavikt", "Vegan"))
        .addKdoc("Can make it vegan!")
        .addModifiers(KModifier.PUBLIC)
        .addTypeVariable(TypeVariableName("T"))
        .delegate("Delegates.notNull()")
        .contextParameter("user", STRING)
        .receiver(Int::class)
        .getter(
          FunSpec.getterBuilder()
            .addModifiers(KModifier.INLINE)
            .addStatement("return %S", 42)
            .build()
        )
        .setter(
          FunSpec.setterBuilder()
            .addModifiers(KModifier.INLINE)
            .addParameter("value", Int::class)
            .build()
        )
        .addOriginatingElement(originatingElement)
        .build()

    val newProp = prop.toBuilder().build()
    assertThat(newProp).isEqualTo(prop)
    assertThat(newProp.originatingElements).containsExactly(originatingElement)
  }

  @Test
  fun typeVariablesWithWhere() {
    val t = TypeVariableName("T", Serializable::class, Cloneable::class)
    val r = TypeVariableName("R", Any::class)
    val function = Function::class.asClassName().parameterizedBy(t, r)
    val prop =
      PropertySpec.builder("property", String::class, PRIVATE)
        .receiver(function)
        .addTypeVariables(listOf(t, r))
        .getter(FunSpec.getterBuilder().addStatement("return %S", "").build())
        .build()
    assertThat(prop.toString())
      .isEqualTo(
        """
        |private val <T, R : kotlin.Any> java.util.function.Function<T, R>.`property`: kotlin.String where T : java.io.Serializable, T : kotlin.Cloneable
        |  get() = ""
        |"""
          .trimMargin()
      )
  }

  @OptIn(DelicateKaviApi::class)
  @Test
  fun annotatedValWithContextReceiver() {
    val propertySpec =
      PropertySpec.builder("foo", INT)
        .mutable(false)
        .addAnnotation(AnnotationSpec.get(TestAnnotation()))
        .contextReceivers(STRING)
        .getter(FunSpec.getterBuilder().addStatement("return length").build())
        .build()

    assertThat(propertySpec.toString())
      .isEqualTo(
        """
        |context(kotlin.String)
        |@io.github.kavikt.PropertySpecJvmTest.TestAnnotation
        |val foo: kotlin.Int
        |  get() = length
        |"""
          .trimMargin()
      )
  }

  @OptIn(DelicateKaviApi::class)
  @Test
  fun annotatedValWithContextParameter() {
    val propertySpec =
      PropertySpec.builder("foo", INT)
        .mutable(false)
        .addAnnotation(AnnotationSpec.get(TestAnnotation()))
        .contextParameter("str", STRING)
        .getter(FunSpec.getterBuilder().addStatement("return str.length").build())
        .build()

    assertThat(propertySpec.toString())
      .isEqualTo(
        """
        |context(str: kotlin.String)
        |@io.github.kavikt.PropertySpecJvmTest.TestAnnotation
        |val foo: kotlin.Int
        |  get() = str.length
        |"""
          .trimMargin()
      )
  }
}
