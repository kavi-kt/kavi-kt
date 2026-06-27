/*
 * Copyright (C) 2019 Square, Inc.
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
import assertk.assertions.isNull
import io.github.kavikt.KModifier.CROSSINLINE
import kotlin.test.Test

class TaggableTest {

  private fun builders(): List<Taggable.Builder<*>> =
    listOf(
      AnnotationSpec.builder(ClassName("kotlin", "Suppress")),
      FunSpec.builder("test"),
      ParameterSpec.builder("test", STRING),
      PropertySpec.builder("test", STRING),
      TypeAliasSpec.builder("Test", STRING),
      TypeSpec.classBuilder("Test"),
    )

  @Test
  fun builderShouldMakeDefensiveCopy() {
    for (builder in builders()) {
      builder.tags.clear()
      builder.tag(String::class, "test")
      val taggable = builder.buildTaggable()
      builder.tags.remove(String::class)
      assertThat(taggable.tag<String>()).isEqualTo("test")
    }
  }

  @Test
  fun missingShouldBeNull() {
    for (builder in builders()) {
      builder.tags.clear()
      val taggable = builder.buildTaggable()
      assertThat(taggable.tag<Int>()).isNull()
    }
  }

  @Test
  fun kclassParamFlow() {
    for (builder in builders()) {
      builder.tags.clear()
      builder.tag(String::class, "test")
      val taggable = builder.buildTaggable()
      assertThat(taggable.tag(String::class)).isEqualTo("test")
    }
  }

  // kmp: this test was extracted: javaClassParamFlow

  // kmp: this test was extracted: kclassInJavaClassOut

  // kmp: this test was extracted: javaClassInkClassOut

  private fun Taggable.Builder<*>.buildTaggable(): Taggable {
    // Apply blocks test inline builder tag functions don't break the chain. Result is discarded
    return when (this) {
      is AnnotationSpec.Builder ->
        build().apply { toBuilder().tag(1).addMember(CodeBlock.of("")).build() }
      is FunSpec.Builder -> build().apply { toBuilder().tag(1).returns(STRING).build() }
      is ParameterSpec.Builder ->
        build().apply { toBuilder().tag(1).addModifiers(CROSSINLINE).build() }
      is PropertySpec.Builder ->
        build().apply { toBuilder().tag(1).initializer(CodeBlock.of("")).build() }
      is TypeAliasSpec.Builder ->
        build().apply { toBuilder().tag(1).addKdoc(CodeBlock.of("")).build() }
      is TypeSpec.Builder -> build().apply { toBuilder().tag(1).addKdoc(CodeBlock.of("")).build() }
      else -> TODO("Unsupported type ${this::class.simpleName}")
    }
  }
}
