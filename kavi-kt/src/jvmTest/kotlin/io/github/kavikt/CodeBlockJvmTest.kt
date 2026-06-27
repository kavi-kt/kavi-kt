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
import assertk.assertions.isEqualTo
import java.util.Locale
import kotlin.test.Test

class CodeBlockJvmTest {
  @Test
  fun multipleNamedArguments() {
    val map = LinkedHashMap<String, Any>()
    map["pipe"] = System::class
    map["text"] = "tacos"

    val block =
      CodeBlock.builder().addNamed("%pipe:T.out.println(\"Let's eat some %text:L\");", map).build()

    assertThat(block.toString())
      .isEqualTo("java.lang.System.out.println(\"Let's eat some tacos\");")
  }

  @Test
  fun namedNewline() {
    val map = LinkedHashMap<String, Any>()
    map["clazz"] = java.lang.Integer::class
    val block = CodeBlock.builder().addNamed("%clazz:T\n", map).build()
    assertThat(block.toString()).isEqualTo("kotlin.Int\n")
  }

  @Test
  fun sameIndexCanBeUsedWithDifferentFormats() {
    val block = CodeBlock.builder().add("%1T.out.println(%1S)", System::class.asClassName()).build()
    assertThat(block.toString()).isEqualTo("java.lang.System.out.println(\"java.lang.System\")")
  }

  // https://github.com/square/kotlinpoet/issues/1657
  @Test
  fun minusSignInSwedishLocale() {
    val defaultLocale = Locale.getDefault()
    Locale.setDefault(Locale.forLanguageTag("sv"))

    val i = -42
    val s = CodeBlock.of("val i = %L", i)
    assertThat(s.toString()).isEqualTo("val i = -42")

    Locale.setDefault(defaultLocale)
  }
}
