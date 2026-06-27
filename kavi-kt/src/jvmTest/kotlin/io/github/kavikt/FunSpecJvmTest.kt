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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.google.testing.compile.CompilationRule
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.Callable
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter.methodsIn
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.junit.Rule

@OptIn(ExperimentalKaviApi::class)
class FunSpecJvmTest {
  @Rule @JvmField val compilation = CompilationRule()

  private lateinit var elements: Elements
  private lateinit var types: Types

  @BeforeTest
  fun setUp() {
    elements = compilation.elements
    types = compilation.types
  }

  private fun getElement(`class`: Class<*>): TypeElement {
    return elements.getTypeElement(`class`.canonicalName)
  }

  private fun findFirst(elements: Collection<ExecutableElement>, name: String) =
    elements.firstOrNull { it.simpleName.toString() == name }
      ?: throw IllegalArgumentException("$name not found in $elements")

  @Target(AnnotationTarget.VALUE_PARAMETER) internal annotation class Nullable

  internal abstract class Everything {
    @Deprecated("")
    @Throws(IOException::class, SecurityException::class)
    protected abstract fun <T> everything(@Nullable thing: String, things: List<T>): Runnable
      where T : Runnable, T : Closeable
  }

  internal abstract class HasAnnotation {
    abstract override fun toString(): String
  }

  internal interface ExtendsOthers : Callable<Int>, Comparable<Long>

  annotation class TestAnnotation

  abstract class InvalidOverrideMethods {
    fun finalMethod() {}

    private fun privateMethod() {}

    companion object {
      @Suppress("NON_FINAL_MEMBER_IN_OBJECT") @JvmStatic open fun staticMethod() {}
    }
  }

  @Test
  fun overrideEverything() {
    val classElement = getElement(Everything::class.java)
    val methodElement = methodsIn(classElement.enclosedElements).single()
    val funSpec = FunSpec.overriding(methodElement).build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |@kotlin.jvm.Throws(java.io.IOException::class, java.lang.SecurityException::class)
        |protected override fun <T> everything(arg0: java.lang.String, arg1: java.util.List<out T>): java.lang.Runnable where T : java.lang.Runnable, T : java.io.Closeable {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun overrideDoesNotCopyOverrideAnnotation() {
    val classElement = getElement(HasAnnotation::class.java)
    val exec = methodsIn(classElement.enclosedElements).single()
    val funSpec = FunSpec.overriding(exec).build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public override fun toString(): java.lang.String {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun overrideExtendsOthersWorksWithActualTypeParameters() {
    val classElement = getElement(ExtendsOthers::class.java)
    val classType = classElement.asType() as DeclaredType
    val methods = methodsIn(elements.getAllMembers(classElement))
    var exec = findFirst(methods, "call")

    @Suppress("DEPRECATION") var funSpec = FunSpec.overriding(exec, classType, types).build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |@kotlin.jvm.Throws(java.lang.Exception::class)
        |public override fun call(): java.lang.Integer {
        |}
        |"""
          .trimMargin()
      )
    exec = findFirst(methods, "compareTo")
    @Suppress("DEPRECATION")
    funSpec = FunSpec.overriding(exec, classType, types).build()
    assertThat(funSpec.toString())
      .isEqualTo(
        """
        |public override fun compareTo(arg0: java.lang.Long): kotlin.Int {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun overrideInvalidModifiers() {
    val classElement = getElement(InvalidOverrideMethods::class.java)
    val methods = methodsIn(elements.getAllMembers(classElement))

    assertFailure { FunSpec.overriding(findFirst(methods, "finalMethod")) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("cannot override method with modifiers: [public, final]")

    assertFailure { FunSpec.overriding(findFirst(methods, "privateMethod")) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("cannot override method with modifiers: [private, final]")

    assertFailure { FunSpec.overriding(findFirst(methods, "staticMethod")) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("cannot override method with modifiers: [public, static]")
  }

  @Test
  fun originatingElementToBuilder() {
    val originatingElement = FakeElement()
    val funSpec = FunSpec.builder("foo").addOriginatingElement(originatingElement).build()

    val newSpec = funSpec.toBuilder().build()
    assertThat(newSpec.originatingElements).containsExactly(originatingElement)
  }

  @Test
  fun equalsAndHashCodeForOverriding() {
    val classElement = getElement(Everything::class.java)
    val methodElement = methodsIn(classElement.enclosedElements).single()
    val a = FunSpec.overriding(methodElement).build()
    val b = FunSpec.overriding(methodElement).build()
    assertThat(a == b).isTrue()
    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }

  @Test
  fun jvmStaticModifier() {
    val builder = FunSpec.builder("staticMethod")
    builder.jvmModifiers(listOf(Modifier.STATIC))

    assertThat(builder.build().toString())
      .isEqualTo(
        """
        |@kotlin.jvm.JvmStatic
        |internal fun staticMethod() {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun jvmFinalModifier() {
    val builder = FunSpec.builder("finalMethod")
    builder.jvmModifiers(listOf(Modifier.FINAL))

    assertThat(builder.build().toString())
      .isEqualTo(
        """
        |internal final fun finalMethod() {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun jvmSynchronizedModifier() {
    val builder = FunSpec.builder("synchronizedMethod")
    builder.jvmModifiers(listOf(Modifier.SYNCHRONIZED))

    assertThat(builder.build().toString())
      .isEqualTo(
        """
        |@kotlin.jvm.Synchronized
        |internal fun synchronizedMethod() {
        |}
        |"""
          .trimMargin()
      )
  }
}
