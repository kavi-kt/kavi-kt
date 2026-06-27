/*
 * Copyright (C) 2023 Square, Inc.
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
import io.github.kavikt.WildcardTypeName.Companion.consumerOf
import io.github.kavikt.WildcardTypeName.Companion.producerOf
import kotlin.test.Test

class WildcardTypeNameTest {

  @Test
  fun equalsAndHashCode() {
    val anyProducer1 = producerOf(ANY)
    val anyProducer2 = producerOf(ANY)
    assertThat(anyProducer1).isEqualTo(anyProducer2)
    assertThat(anyProducer1.hashCode()).isEqualTo(anyProducer2.hashCode())
    assertThat(anyProducer1.toString()).isEqualTo(anyProducer2.toString())

    val stringConsumer1 = consumerOf(STRING)
    val stringConsumer2 = consumerOf(STRING)
    assertThat(stringConsumer1).isEqualTo(stringConsumer2)
    assertThat(stringConsumer1.hashCode()).isEqualTo(stringConsumer2.hashCode())
    assertThat(stringConsumer1.toString()).isEqualTo(stringConsumer2.toString())
  }

  @Test
  fun equalsDifferentiatesNullabilityAndAnnotations() {
    val anyProducer = producerOf(ANY)

    assertThat(anyProducer.copy(nullable = true)).isNotEqualTo(anyProducer)

    assertThat(
        anyProducer.copy(
          annotations = listOf(AnnotationSpec.builder(ClassName("kotlin", "Suppress")).build())
        )
      )
      .isNotEqualTo(anyProducer)
  }

  @Test
  fun equalsAndHashCodeIgnoreTags() {
    val anyProducer = producerOf(ANY)
    val tagged = anyProducer.copy(tags = mapOf(String::class to "test"))

    assertThat(anyProducer).isEqualTo(tagged)
    assertThat(anyProducer.hashCode()).isEqualTo(tagged.hashCode())
  }

  @Test
  fun starProjection() {
    assertThat(STAR.toString()).isEqualTo("*")
  }
}
