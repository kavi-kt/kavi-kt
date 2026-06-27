/*
 * Copyright (C) 2014 Google, Inc.
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
import kotlin.test.Test

class ClassNameTest {
  // kmp: this test was extracted: bestGuessForString_simpleClass

  // kmp: this test was extracted: bestGuessNonAscii

  // kmp: this test was extracted: bestGuessForString_nestedClass

  @Test fun bestGuessForString_defaultPackage() {
    assertThat(ClassName.bestGuess("SomeClass"))
      .isEqualTo(ClassName("", "SomeClass"))
    assertThat(ClassName.bestGuess("SomeClass.Nested"))
      .isEqualTo(ClassName("", "SomeClass", "Nested"))
    assertThat(ClassName.bestGuess("SomeClass.Nested.EvenMore"))
      .isEqualTo(ClassName("", "SomeClass", "Nested", "EvenMore"))
  }

  @Test fun bestGuessForString_confusingInput() {
    assertBestGuessThrows("")
    assertBestGuessThrows(".")
    assertBestGuessThrows(".Map")
    assertBestGuessThrows("java")
    assertBestGuessThrows("java.util")
    assertBestGuessThrows("java.util.")
    assertBestGuessThrows("java..util.Map.Entry")
    assertBestGuessThrows("java.util..Map.Entry")
    assertBestGuessThrows("kotlin.collections.Map..Entry")
    assertBestGuessThrows("com.test.$")
    assertBestGuessThrows("com.test.LooksLikeAClass.pkg")
    assertBestGuessThrows("!@#\$gibberish%^&*")
  }

  private fun assertBestGuessThrows(s: String) {
    assertFailure {
      ClassName.bestGuess(s)
    }.isInstanceOf<IllegalArgumentException>()
  }

  @Test fun createNestedClass() {
    val foo = ClassName("com.example", "Foo")
    val bar = foo.nestedClass("Bar")
    assertThat(bar).isEqualTo(ClassName("com.example", "Foo", "Bar"))
    val baz = bar.nestedClass("Baz")
    assertThat(baz).isEqualTo(ClassName("com.example", "Foo", "Bar", "Baz"))
  }

  // kmp: this test was extracted: classNameFromTypeElement

  // kmp: this test was extracted: classNameFromClass

  // kmp: this test was extracted: classNameFromKClass

  // kmp: this test was extracted: classNameFromKClassSpecialCases

  // kmp: this test was extracted: peerClass

  // kmp: this test was extracted: fromClassRejectionTypes

  @Suppress("DEPRECATION_ERROR") // Ensure still throws in case called from Java.
  @Test fun fromEmptySimpleName() {
    assertFailure {
      ClassName("foo" /* no simple name */)
    }.isInstanceOf<IllegalArgumentException>()
  }

  // kmp: this test was extracted: reflectionName

  // kmp: this test was extracted: constructorReferences

  @Test fun spacesEscaping() {
    val tacoFactory = ClassName("com.squareup.taco factory", "Taco Factory")
    val file = FileSpec.builder("com.squareup.tacos", "TacoTest")
      .addFunction(
        FunSpec.builder("main")
          .addStatement("println(%T.produceTacos())", tacoFactory)
          .build()
      )
      .build()
    assertThat(file.toString()).isEqualTo(
      """
      |package com.squareup.tacos
      |
      |import com.squareup.`taco factory`.`Taco Factory`
      |
      |public fun main() {
      |  println(`Taco Factory`.produceTacos())
      |}
      |""".trimMargin()
    )
  }

  @Test fun emptySimpleNamesForbidden() {
    assertFailure {
      ClassName(packageName = "", simpleNames = emptyArray())
    }.isInstanceOf<IllegalArgumentException>()
      .hasMessage("simpleNames must not be empty")

    assertFailure {
      ClassName(packageName = "", simpleNames = arrayOf("Foo", "Bar", ""))
    }.isInstanceOf<IllegalArgumentException>()
      .hasMessage(
      "simpleNames must not contain empty items: " +
        "[Foo, Bar, ]"
    )

    assertFailure {
      ClassName(packageName = "", simpleNames = emptyList())
    }.isInstanceOf<IllegalArgumentException>()
      .hasMessage("simpleNames must not be empty")

    assertFailure {
      ClassName(packageName = "", simpleNames = listOf("Foo", "Bar", ""))
    }.isInstanceOf<IllegalArgumentException>()
      .hasMessage(
      "simpleNames must not contain empty items: " +
        "[Foo, Bar, ]"
    )
  }

  @Test fun equalsAndHashCode() {
    val foo1 = ClassName(names = listOf("com.example", "Foo"))
    val foo2 = ClassName(names = listOf("com.example", "Foo"))
    assertThat(foo1).isEqualTo(foo2)
    assertThat(foo1.hashCode()).isEqualTo(foo2.hashCode())
  }

  @Test fun equalsDifferentiatesPackagesFromSimpleNames() {
    val outerFoo = ClassName("com.example.Foo", "Bar")
    val packageFoo = ClassName("com.example", "Foo", "Bar")

    assertThat(outerFoo).isNotEqualTo(packageFoo)
  }

  @Test fun equalsDifferentiatesNullabilityAndAnnotations() {
    val foo = ClassName(names = listOf("com.example", "Foo"))
    assertThat(
        foo.copy(
          annotations = listOf(AnnotationSpec.Builder(ClassName("kotlin", "Suppress")).build())
        )
      )
      .isNotEqualTo(foo)
    assertThat(foo.copy(nullable = true)).isNotEqualTo(foo)
  }

  @Test fun equalsAndHashCodeIgnoreTags() {
    val foo = ClassName(names = listOf("com.example", "Foo"))
    val taggedFoo = foo.copy(tags = mapOf(String::class to "test"))

    assertThat(foo).isEqualTo(taggedFoo)
    assertThat(foo.hashCode()).isEqualTo(taggedFoo.hashCode())
  }

  @Test fun compareTo() {
    val robot = ClassName("com.example", "Robot")
    val robotMotor = ClassName("com.example", "Robot", "Motor")
    val roboticVacuum = ClassName("com.example", "RoboticVacuum")

    val list = listOf(robot, robotMotor, roboticVacuum)

    assertThat(list.sorted()).isEqualTo(listOf(robot, robotMotor, roboticVacuum))
  }

  @Test fun compareToConsistentWithEquals() {
    val foo1 = ClassName(names = listOf("com.example", "Foo"))
    val foo2 = ClassName(names = listOf("com.example", "Foo"))
    assertThat(foo1.compareTo(foo2)).isEqualTo(0)
  }

  @Test fun compareToDifferentiatesPackagesFromSimpleNames() {
    val parentFooNestedBar = ClassName("com.example", "Foo", "Bar")
    val packageFooClassBar = ClassName("com.example.Foo", "Bar")
    val parentFooNestedBaz = ClassName("com.example", "Foo", "Baz")
    val packageFooClassBaz = ClassName("com.example.Foo", "Baz")
    val parentGooNestedBar = ClassName("com.example", "Goo", "Bar")
    val packageGooClassBar = ClassName("com.example.Goo", "Bar")

    val list = listOf(
      parentFooNestedBar,
      packageFooClassBar,
      parentFooNestedBaz,
      packageFooClassBaz,
      parentGooNestedBar,
      packageGooClassBar,
    )

    assertThat(list.sorted()).isEqualTo(
      listOf(
        parentFooNestedBar,
        parentFooNestedBaz,
        parentGooNestedBar,
        packageFooClassBar,
        packageFooClassBaz,
        packageGooClassBar,
      ),
    )
  }

  @Test fun compareToDifferentiatesNullabilityAndAnnotations() {
    val plain = ClassName(listOf("com.example", "Foo"))
    val nullable =
      ClassName(
        listOf("com.example", "Foo"),
        nullable = true,
      )
    val annotated =
      ClassName(
        listOf("com.example", "Foo"),
        nullable = true,
        annotations = listOf(AnnotationSpec.Builder(ClassName("kotlin", "Suppress")).build()),
      )

    val list = listOf(plain, nullable, annotated)

    assertThat(list.sorted()).isEqualTo(listOf(plain, nullable, annotated))
  }

  @Test fun compareToDifferentiatesByAnnotation() {
    val noAnnotations = ClassName(listOf("com.example", "Foo"))

    val oneAnnotation =
      ClassName(
        listOf("com.example", "Foo"),
        annotations = listOf(AnnotationSpec.Builder(ClassName("kotlin", "Suppress")).build()),
      )
    val twoAnnotations =
      ClassName(
        listOf("com.example", "Foo"),
        annotations =
          listOf(
            AnnotationSpec.Builder(ClassName("kotlin", "Suppress")).build(),
            AnnotationSpec.Builder(ClassName("kotlin.test", "Test")).build(),
          ),
      )
    val secondAnnotationOnly =
      ClassName(
        listOf("com.example", "Foo"),
        annotations = listOf(AnnotationSpec.Builder(ClassName("kotlin.test", "Test")).build()),
      )

    val list = listOf(noAnnotations, oneAnnotation, twoAnnotations, secondAnnotationOnly)

    assertThat(list.sorted())
      .isEqualTo(listOf(noAnnotations, oneAnnotation, twoAnnotations, secondAnnotationOnly))
  }

  @Test fun compareToDoesNotDifferentiateByTag() {
    val noTags = ClassName(listOf("com.example", "Foo"))

    val oneTag = ClassName(
      listOf("com.example", "Foo"),
      tags = mapOf(String::class to "test"),
    )
    val twoTags = ClassName(
      listOf("com.example", "Foo"),
      tags = mapOf(String::class to "test", Int::class to 1),
    )

    assertThat(noTags.compareTo(oneTag)).isEqualTo(0)
    assertThat(oneTag.compareTo(twoTags)).isEqualTo(0)
  }
}
