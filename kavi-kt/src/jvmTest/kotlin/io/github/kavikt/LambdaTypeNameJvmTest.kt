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
import kotlin.test.Test

@OptIn(ExperimentalKaviApi::class)
class LambdaTypeNameJvmTest {

  @Retention(AnnotationRetention.RUNTIME) annotation class HasSomeAnnotation

  @HasSomeAnnotation inner class IsAnnotated

  @Test
  fun receiverWithAnnotationHasParens() {
    val annotation = IsAnnotated::class.java.getAnnotation(HasSomeAnnotation::class.java)
    val typeName =
      LambdaTypeName.get(
        receiver =
          Int::class.asClassName()
            .copy(
              annotations = listOf(AnnotationSpec.get(annotation, includeDefaultValues = true))
            ),
        parameters = listOf(),
        returnType = Unit::class.asTypeName(),
      )
    assertThat(typeName.toString())
      .isEqualTo(
        "(@io.github.kavikt.LambdaTypeNameJvmTest.HasSomeAnnotation kotlin.Int).() -> kotlin.Unit"
      )
  }

  @Test
  fun functionWithAnnotatedContextReceiver() {
    val annotatedType =
      STRING.copy(annotations = listOf(AnnotationSpec.get(FunSpecTest.TestAnnotation())))
    val typeName =
      LambdaTypeName.get(
        Int::class.asTypeName(),
        listOf(),
        Unit::class.asTypeName(),
        listOf(annotatedType),
      )

    assertThat(typeName.toString())
      .isEqualTo(
        "context(@io.github.kavikt.FunSpecTest.TestAnnotation kotlin.String) kotlin.Int.() -> kotlin.Unit"
      )
  }

  @Test
  fun functionWithAnnotatedContextParameter() {
    val annotatedType =
      STRING.copy(annotations = listOf(AnnotationSpec.get(FunSpecTest.TestAnnotation())))
    val typeName =
      LambdaTypeName.get(
        Int::class.asTypeName(),
        listOf(),
        Unit::class.asTypeName(),
        contextParameters = listOf(annotatedType),
      )

    assertThat(typeName.toString())
      .isEqualTo(
        "context(@io.github.kavikt.FunSpecTest.TestAnnotation kotlin.String) kotlin.Int.() -> kotlin.Unit"
      )
  }
}
