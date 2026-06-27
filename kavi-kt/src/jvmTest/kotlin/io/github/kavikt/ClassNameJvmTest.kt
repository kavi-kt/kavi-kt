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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.google.testing.compile.CompilationRule
import io.github.kavikt.Cased.Weird.Sup
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule

class ClassNameJvmTest {
  @Rule @JvmField var compilationRule = CompilationRule()

  @Test
  fun bestGuessForString_simpleClass() {
    assertThat(ClassName.bestGuess(String::class.java.name))
      .isEqualTo(ClassName("java.lang", "String"))
  }

  @Test
  fun bestGuessNonAscii() {
    val className = ClassName.bestGuess("com.𝐚ndro𝐢d.𝐀ctiv𝐢ty")
    assertEquals("com.𝐚ndro𝐢d", className.packageName)
    assertEquals("𝐀ctiv𝐢ty", className.simpleName)
  }

  internal class OuterClass {
    internal class InnerClass
  }

  @Test
  fun bestGuessForString_nestedClass() {
    assertThat(ClassName.bestGuess(Map.Entry::class.java.canonicalName))
      .isEqualTo(ClassName("java.util", "Map", "Entry"))
    assertThat(ClassName.bestGuess(OuterClass.InnerClass::class.java.canonicalName))
      .isEqualTo(
        ClassName(
          "io.github.kavikt",
          "ClassNameJvmTest",
          "OuterClass",
          "InnerClass",
        )
      )
  }

  @Test
  fun classNameFromTypeElement() {
    val elements = compilationRule.elements
    val element = elements.getTypeElement(Any::class.java.canonicalName)
    assertThat(element.asClassName().toString()).isEqualTo("java.lang.Object")
  }

  @Test
  fun classNameFromClass() {
    assertThat(Any::class.java.asClassName().toString()).isEqualTo("java.lang.Object")
    assertThat(OuterClass.InnerClass::class.java.asClassName().toString())
      .isEqualTo("io.github.kavikt.ClassNameJvmTest.OuterClass.InnerClass")
  }

  @Test
  fun classNameFromKClass() {
    assertThat(Any::class.asClassName().toString()).isEqualTo("kotlin.Any")
    assertThat(OuterClass.InnerClass::class.asClassName().toString())
      .isEqualTo("io.github.kavikt.ClassNameJvmTest.OuterClass.InnerClass")

    // Note: Do NOT rewrite this assertion to something more clever since behaviors
    //  like TypeName.equals may subvert the correct partitioning of package and names.
    val hi = Sup.Hi::class.asClassName()
    assertThat(hi.packageName).isEqualTo("io.github.kavikt.Cased.Weird")
    assertThat(hi.simpleNames).containsExactly("Sup", "Hi")
  }

  @Test
  fun classNameFromKClassSpecialCases() {
    assertEquals(
      ClassName(listOf("kotlin", "Boolean", "Companion")),
      Boolean.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Byte", "Companion")),
      Byte.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Char", "Companion")),
      Char.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Double", "Companion")),
      Double.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Enum", "Companion")),
      Enum.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Float", "Companion")),
      Float.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Int", "Companion")),
      Int.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Long", "Companion")),
      Long.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "Short", "Companion")),
      Short.Companion::class.asClassName(),
    )
    assertEquals(
      ClassName(listOf("kotlin", "String", "Companion")),
      String.Companion::class.asClassName(),
    )
  }

  @Test
  fun peerClass() {
    assertThat(java.lang.Double::class.asClassName().peerClass("Short"))
      .isEqualTo(java.lang.Short::class.asClassName())
    assertThat(ClassName("", "Double").peerClass("Short")).isEqualTo(ClassName("", "Short"))
    assertThat(ClassName("a.b", "Combo", "Taco").peerClass("Burrito"))
      .isEqualTo(ClassName("a.b", "Combo", "Burrito"))
  }

  @Test
  fun fromClassRejectionTypes() {
    assertFailure {
        java.lang.Integer.TYPE.asClassName()
      }
      .isInstanceOf<IllegalArgumentException>()

    assertFailure {
        Void.TYPE.asClassName()
      }
      .isInstanceOf<IllegalArgumentException>()

    assertFailure {
        Array<Any>::class.java.asClassName()
      }
      .isInstanceOf<IllegalArgumentException>()

    // TODO
    // assertFailure {
    //  Array<Int>::class.asClassName()
    // }.isInstanceOf<IllegalArgumentException>()
  }

  @Test
  fun reflectionName() {
    assertThat(Thread.State::class.asClassName().reflectionName())
      .isEqualTo("java.lang.Thread\$State")
  }

  @Test
  fun constructorReferences() {
    assertThat(Thread.State::class.asClassName().constructorReference().toString())
      .isEqualTo("java.lang.Thread::State")
  }
}
