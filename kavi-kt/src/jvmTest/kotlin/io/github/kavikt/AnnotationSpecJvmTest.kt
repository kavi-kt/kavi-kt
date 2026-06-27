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
import com.google.testing.compile.CompilationRule
import java.lang.annotation.Inherited
import kotlin.reflect.KClass
import kotlin.test.Test
import org.junit.Rule

class AnnotationSpecJvmTest {

  @Retention(AnnotationRetention.RUNTIME) annotation class AnnotationA

  @Inherited @Retention(AnnotationRetention.RUNTIME) annotation class AnnotationB

  @Retention(AnnotationRetention.RUNTIME) annotation class AnnotationC(val value: String)

  enum class Breakfast {
    WAFFLES,
    PANCAKES;

    override fun toString(): String {
      return "$name with cherries!"
    }
  }

  @Retention(AnnotationRetention.RUNTIME)
  annotation class HasDefaultsAnnotation(
    val a: Byte = 5,
    val b: Short = 6,
    val c: Int = 7,
    val d: Long = 8,
    val e: Float = 9.0f,
    val f: Double = 10.0,
    val g: CharArray = ['\u0000', '\uCAFE', 'z', '€', 'ℕ', '"', '\'', '\t', '\n'],
    val h: Boolean = true,
    val i: Breakfast = Breakfast.WAFFLES,
    val j: AnnotationA = AnnotationA(),
    val k: String = "maple",
    val l: KClass<out Annotation> = AnnotationB::class,
    val m: IntArray = [1, 2, 3],
    val n: Array<Breakfast> = [Breakfast.WAFFLES, Breakfast.PANCAKES],
    val o: Breakfast,
    val p: Int,
    val q: AnnotationC = AnnotationC("foo"),
    val r: Array<KClass<out Number>> = [Byte::class, Short::class, Int::class, Long::class],
    val s: Array<AnnotationC> = [AnnotationC("foo"), AnnotationC("bar")],
  )

  @HasDefaultsAnnotation(
    o = Breakfast.PANCAKES,
    p = 1701,
    f = 11.1,
    m = [9, 8, 1],
    l = Override::class,
    j = AnnotationA(),
    q = AnnotationC("bar"),
    r = [Float::class, Double::class],
    s = [AnnotationC("bar")],
  )
  inner class IsAnnotated

  @Rule @JvmField val compilation = CompilationRule()

  @Test
  fun defaultAnnotation() {
    val name = IsAnnotated::class.java.canonicalName
    val element = compilation.elements.getTypeElement(name)
    val annotation = AnnotationSpec.get(element.annotationMirrors[0])

    assertThat(toString(annotation))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.AnnotationSpecJvmTest
        |import java.lang.Override
        |import kotlin.Double
        |import kotlin.Float
        |
        |@AnnotationSpecJvmTest.HasDefaultsAnnotation(
        |  f = 11.1,
        |  j = AnnotationSpecJvmTest.AnnotationA(),
        |  l = Override::class,
        |  m = arrayOf(9, 8, 1),
        |  o = AnnotationSpecJvmTest.Breakfast.PANCAKES,
        |  p = 1_701,
        |  q = AnnotationSpecJvmTest.AnnotationC(value = "bar"),
        |  r = arrayOf(Float::class, Double::class),
        |  s = arrayOf(AnnotationSpecJvmTest.AnnotationC(value = "bar")),
        |)
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun defaultAnnotationWithImport() {
    val name = IsAnnotated::class.java.canonicalName
    val element = compilation.elements.getTypeElement(name)
    val annotation = AnnotationSpec.get(element.annotationMirrors[0])
    val typeBuilder = TypeSpec.classBuilder(IsAnnotated::class.java.simpleName)
    typeBuilder.addAnnotation(annotation)
    val file = FileSpec.get("io.github.kavikt", typeBuilder.build())
    assertThat(file.toString())
      .isEqualTo(
        """
        |package io.github.kavikt
        |
        |import java.lang.Override
        |import kotlin.Double
        |import kotlin.Float
        |
        |@AnnotationSpecJvmTest.HasDefaultsAnnotation(
        |  f = 11.1,
        |  j = AnnotationSpecJvmTest.AnnotationA(),
        |  l = Override::class,
        |  m = arrayOf(9, 8, 1),
        |  o = AnnotationSpecJvmTest.Breakfast.PANCAKES,
        |  p = 1_701,
        |  q = AnnotationSpecJvmTest.AnnotationC(value = "bar"),
        |  r = arrayOf(Float::class, Double::class),
        |  s = arrayOf(AnnotationSpecJvmTest.AnnotationC(value = "bar")),
        |)
        |public class IsAnnotated
        |"""
          .trimMargin()
      )
  }

  @Test
  fun emptyArray() {
    val builder = AnnotationSpec.builder(HasDefaultsAnnotation::class.java)
    builder.addMember("%L = %L", "n", "[]")
    assertThat(builder.build().toString())
      .isEqualTo(
        "" + "@io.github.kavikt.AnnotationSpecJvmTest.HasDefaultsAnnotation(" + "n = []" + ")"
      )
    builder.addMember("%L = %L", "m", "[]")
    assertThat(builder.build().toString())
      .isEqualTo(
        "" +
          "@io.github.kavikt.AnnotationSpecJvmTest.HasDefaultsAnnotation(" +
          "n = [], " +
          "m = []" +
          ")"
      )
  }

  @Test
  fun reflectAnnotation() {
    val annotation = IsAnnotated::class.java.getAnnotation(HasDefaultsAnnotation::class.java)
    val spec = AnnotationSpec.get(annotation)

    assertThat(toString(spec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.AnnotationSpecJvmTest
        |import java.lang.Override
        |import kotlin.Double
        |import kotlin.Float
        |
        |@AnnotationSpecJvmTest.HasDefaultsAnnotation(
        |  f = 11.1,
        |  l = Override::class,
        |  m = arrayOf(9, 8, 1),
        |  o = AnnotationSpecJvmTest.Breakfast.PANCAKES,
        |  p = 1_701,
        |  q = AnnotationSpecJvmTest.AnnotationC(value = "bar"),
        |  r = arrayOf(Float::class, Double::class),
        |  s = arrayOf(AnnotationSpecJvmTest.AnnotationC(value = "bar")),
        |)
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun reflectAnnotationWithDefaults() {
    val annotation = IsAnnotated::class.java.getAnnotation(HasDefaultsAnnotation::class.java)
    val spec = AnnotationSpec.get(annotation, true)

    assertThat(toString(spec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.AnnotationSpecJvmTest
        |import java.lang.Override
        |import kotlin.Double
        |import kotlin.Float
        |
        |@AnnotationSpecJvmTest.HasDefaultsAnnotation(
        |  a = 5,
        |  b = 6,
        |  c = 7,
        |  d = 8,
        |  e = 9.0f,
        |  f = 11.1,
        |  g = arrayOf('\u0000', '쫾', 'z', '€', 'ℕ', '"', '\'', '\t', '\n'),
        |  h = true,
        |  i = AnnotationSpecJvmTest.Breakfast.WAFFLES,
        |  j = AnnotationSpecJvmTest.AnnotationA(),
        |  k = "maple",
        |  l = Override::class,
        |  m = arrayOf(9, 8, 1),
        |  n = arrayOf(AnnotationSpecJvmTest.Breakfast.WAFFLES, AnnotationSpecJvmTest.Breakfast.PANCAKES),
        |  o = AnnotationSpecJvmTest.Breakfast.PANCAKES,
        |  p = 1_701,
        |  q = AnnotationSpecJvmTest.AnnotationC(value = "bar"),
        |  r = arrayOf(Float::class, Double::class),
        |  s = arrayOf(AnnotationSpecJvmTest.AnnotationC(value = "bar")),
        |)
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun getOnVarargMirrorShouldNameValueArg() {
    val myClazz =
      compilation.elements.getTypeElement(KotlinClassWithVarargAnnotation::class.java.canonicalName)
    val classBuilder = TypeSpec.classBuilder("Result")

    myClazz.annotationMirrors
      .map { AnnotationSpec.get(it) }
      .filter {
        val typeName = it.typeName
        return@filter typeName is ClassName && typeName.simpleName == "AnnotationWithArrayValue"
      }
      .forEach { classBuilder.addAnnotation(it) }

    assertThat(toString(classBuilder.build()).trim())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.AnnotationSpecJvmTest
        |import java.lang.Object
        |import kotlin.Boolean
        |
        |@AnnotationSpecJvmTest.AnnotationWithArrayValue(value = arrayOf(Object::class, Boolean::class))
        |public class Result
        """
          .trimMargin()
      )
  }

  @Test
  fun getOnVarargAnnotationShouldNameValueArg() {
    val annotation =
      KotlinClassWithVarargAnnotation::class
        .java
        .getAnnotation(AnnotationWithArrayValue::class.java)
    val classBuilder = TypeSpec.classBuilder("Result").addAnnotation(AnnotationSpec.get(annotation))

    assertThat(toString(classBuilder.build()).trim())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.AnnotationSpecJvmTest
        |import java.lang.Object
        |import kotlin.Boolean
        |
        |@AnnotationSpecJvmTest.AnnotationWithArrayValue(value = arrayOf(Object::class, Boolean::class))
        |public class Result
        """
          .trimMargin()
      )
  }

  @AnnotationWithArrayValue(Any::class, Boolean::class) class KotlinClassWithVarargAnnotation

  @Retention(AnnotationRetention.RUNTIME)
  internal annotation class AnnotationWithArrayValue(vararg val value: KClass<*>)

  private fun toString(annotationSpec: AnnotationSpec) =
    toString(TypeSpec.classBuilder("Taco").addAnnotation(annotationSpec).build())

  private fun toString(typeSpec: TypeSpec) = FileSpec.get("com.squareup.tacos", typeSpec).toString()
}
