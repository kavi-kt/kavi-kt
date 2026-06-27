/*
 * Copyright (C) 2014 Google, Inc.
 * Copyright (C) 2025 Square, Inc.
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

// KClass.asClassName() conversion is the subject here, so these rely on KClass.qualifiedName, which
// is stripped on js/wasm.
class ClassNameTestJvmNative {

  @Test
  fun reflectionName() {
    assertThat(ANY.reflectionName()).isEqualTo("kotlin.Any")
    assertThat(Map.Entry::class.asClassName().reflectionName())
      .isEqualTo("kotlin.collections.Map\$Entry")
    assertThat(ClassName("", "Foo").reflectionName()).isEqualTo("Foo")
    assertThat(ClassName("", "Foo", "Bar", "Baz").reflectionName()).isEqualTo("Foo\$Bar\$Baz")
    assertThat(ClassName("a.b.c", "Foo", "Bar", "Baz").reflectionName())
      .isEqualTo("a.b.c.Foo\$Bar\$Baz")
  }

  @Test
  fun constructorReferences() {
    assertThat(String::class.asClassName().constructorReference().toString())
      .isEqualTo("::kotlin.String")
    assertThat(ClassName("", "Foo").constructorReference().toString()).isEqualTo("::Foo")
    assertThat(ClassName("", "Foo", "Bar", "Baz").constructorReference().toString())
      .isEqualTo("Foo.Bar::Baz")
    assertThat(ClassName("a.b.c", "Foo", "Bar", "Baz").constructorReference().toString())
      .isEqualTo("a.b.c.Foo.Bar::Baz")
  }
}
