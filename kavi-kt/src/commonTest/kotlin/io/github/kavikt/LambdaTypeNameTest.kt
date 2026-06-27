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
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import io.github.kavikt.KModifier.VARARG
import kotlin.test.Test

@OptIn(ExperimentalKaviApi::class)
class LambdaTypeNameTest {

  @Test
  fun receiverWithoutAnnotationHasNoParens() {
    val typeName = LambdaTypeName.get(receiver = INT, parameters = listOf(), returnType = UNIT)
    assertThat(typeName.toString()).isEqualTo("kotlin.Int.() -> kotlin.Unit")
  }

  // kmp: this test was extracted: receiverWithAnnotationHasParens

  @Test
  fun contextReceiver() {
    val typeName =
      LambdaTypeName.get(
        receiver = INT,
        parameters = listOf(),
        returnType = UNIT,
        contextReceivers = listOf(STRING),
      )
    assertThat(typeName.toString()).isEqualTo("context(kotlin.String) kotlin.Int.() -> kotlin.Unit")
  }

  @Test
  fun nullableFunctionWithContextReceiver() {
    val typeName =
      LambdaTypeName.get(
          receiver = INT,
          parameters = listOf(),
          returnType = UNIT,
          contextReceivers = listOf(STRING),
        )
        .copy(nullable = true)
    assertThat(typeName.toString())
      .isEqualTo("(context(kotlin.String) kotlin.Int.() -> kotlin.Unit)?")
  }

  @Test
  fun suspendingFunctionWithContextReceiver() {
    val typeName =
      LambdaTypeName.get(
          receiver = INT,
          parameters = listOf(),
          returnType = UNIT,
          contextReceivers = listOf(STRING),
        )
        .copy(suspending = true)
    assertThat(typeName.toString())
      .isEqualTo("suspend context(kotlin.String) kotlin.Int.() -> kotlin.Unit")
  }

  @Test
  fun functionWithMultipleContextReceivers() {
    val typeName =
      LambdaTypeName.get(
        INT,
        listOf(),
        UNIT,
        listOf(STRING, BOOLEAN),
      )
    assertThat(typeName.toString())
      .isEqualTo("context(kotlin.String, kotlin.Boolean) kotlin.Int.() -> kotlin.Unit")
  }

  @Test
  fun functionWithGenericContextReceiver() {
    val genericType = TypeVariableName("T")
    val typeName =
      LambdaTypeName.get(
        INT,
        listOf(),
        UNIT,
        listOf(genericType),
      )

    assertThat(typeName.toString()).isEqualTo("context(T) kotlin.Int.() -> kotlin.Unit")
  }

  // kmp: this test was extracted: functionWithAnnotatedContextReceiver

  @Test
  fun contextParameter() {
    val typeName =
      LambdaTypeName.get(
        receiver = INT,
        parameters = listOf(),
        returnType = UNIT,
        contextParameters = listOf(STRING),
      )
    assertThat(typeName.toString()).isEqualTo("context(kotlin.String) kotlin.Int.() -> kotlin.Unit")
  }

  @Test
  fun nullableFunctionWithContextParameter() {
    val typeName =
      LambdaTypeName.get(
          receiver = INT,
          parameters = listOf(),
          returnType = UNIT,
          contextParameters = listOf(STRING),
        )
        .copy(nullable = true)
    assertThat(typeName.toString())
      .isEqualTo("(context(kotlin.String) kotlin.Int.() -> kotlin.Unit)?")
  }

  @Test
  fun suspendingFunctionWithContextParameter() {
    val typeName =
      LambdaTypeName.get(
          receiver = INT,
          parameters = listOf(),
          returnType = UNIT,
          contextParameters = listOf(STRING),
        )
        .copy(suspending = true)
    assertThat(typeName.toString())
      .isEqualTo("suspend context(kotlin.String) kotlin.Int.() -> kotlin.Unit")
  }

  @Test
  fun functionWithMultipleContextParameters() {
    val typeName =
      LambdaTypeName.get(
        INT,
        listOf(),
        UNIT,
        contextParameters = listOf(STRING, INT),
      )
    assertThat(typeName.toString())
      .isEqualTo("context(kotlin.String, kotlin.Int) kotlin.Int.() -> kotlin.Unit")
  }

  // kmp: this test was extracted: functionWithAnnotatedContextParameter

  @Test
  fun functionWithContextReceiverAndContextParameter() {
    assertFailure {
        LambdaTypeName.get(
          INT,
          listOf(),
          UNIT,
          contextReceivers = listOf(STRING),
          contextParameters = listOf(INT),
        )
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Using both context receivers and context parameters is not allowed")
  }

  @Test
  fun paramsWithAnnotationsForbidden() {
    assertFailure {
        LambdaTypeName.get(
          parameters =
            arrayOf(
              ParameterSpec.builder("foo", INT)
                .addAnnotation(ClassName("kotlin", "Deprecated"))
                .build()
            ),
          returnType = UNIT,
        )
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Parameters with annotations are not allowed")
  }

  @Test
  fun paramsWithModifiersForbidden() {
    assertFailure {
        LambdaTypeName.get(
          parameters = arrayOf(ParameterSpec.builder("foo", INT).addModifiers(VARARG).build()),
          returnType = UNIT,
        )
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Parameters with modifiers are not allowed")
  }

  @Test
  fun paramsWithDefaultValueForbidden() {
    assertFailure {
        LambdaTypeName.get(
          parameters = arrayOf(ParameterSpec.builder("foo", INT).defaultValue("42").build()),
          returnType = UNIT,
        )
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Parameters with default values are not allowed")
  }

  @Test
  fun lambdaReturnType() {
    val returnTypeName = LambdaTypeName.get(parameters = arrayOf(INT), returnType = UNIT)
    val typeName = LambdaTypeName.get(parameters = arrayOf(INT), returnType = returnTypeName)
    assertThat(typeName.toString()).isEqualTo("(kotlin.Int) -> ((kotlin.Int) -> kotlin.Unit)")
  }

  @Test
  fun lambdaParameterType() {
    val parameterTypeName = LambdaTypeName.get(parameters = arrayOf(INT), returnType = INT)
    val typeName = LambdaTypeName.get(parameters = arrayOf(parameterTypeName), returnType = UNIT)
    assertThat(typeName.toString()).isEqualTo("((kotlin.Int) -> kotlin.Int) -> kotlin.Unit")
  }

  @Test
  fun equalsAndHashCode() {
    val lambdaTypeName1 = LambdaTypeName.get(parameters = arrayOf(INT), returnType = INT)
    val lambdaTypeName2 = LambdaTypeName.get(parameters = arrayOf(INT), returnType = INT)
    assertThat(lambdaTypeName1).isEqualTo(lambdaTypeName2)
    assertThat(lambdaTypeName1.hashCode()).isEqualTo(lambdaTypeName2.hashCode())
    assertThat(lambdaTypeName1.toString()).isEqualTo(lambdaTypeName2.toString())

    val differentReceiver =
      LambdaTypeName.get(parameters = arrayOf(INT), returnType = INT, receiver = ANY)
    assertThat(differentReceiver).isNotEqualTo(lambdaTypeName1)

    assertThat(lambdaTypeName1.copy(nullable = true)).isNotEqualTo(lambdaTypeName1)

    assertThat(
        lambdaTypeName1.copy(
          annotations = listOf(AnnotationSpec.builder(ClassName("kotlin", "Suppress")).build())
        )
      )
      .isNotEqualTo(lambdaTypeName1)

    assertThat(lambdaTypeName1.copy(suspending = true)).isNotEqualTo(lambdaTypeName1)
  }

  @Test
  fun equalsAndHashCodeIgnoreTags() {
    val lambdaTypeName = LambdaTypeName.get(parameters = arrayOf(INT), returnType = INT)

    val tagged = lambdaTypeName.copy(tags = mapOf(String::class to "test"))

    assertThat(tagged).isEqualTo(lambdaTypeName)
    assertThat(tagged.hashCode()).isEqualTo(lambdaTypeName.hashCode())
  }
}
