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
import com.google.testing.compile.CompilationRule
import io.github.kavikt.KModifier.ABSTRACT
import io.github.kavikt.KModifier.DATA
import io.github.kavikt.KModifier.IN
import io.github.kavikt.KModifier.INNER
import io.github.kavikt.KModifier.INTERNAL
import io.github.kavikt.KModifier.PRIVATE
import io.github.kavikt.KModifier.PUBLIC
import io.github.kavikt.KModifier.VARARG
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import io.github.kavikt.jvm.throws
import java.io.IOException
import java.io.Serializable
import java.lang.Deprecated
import java.math.BigDecimal
import java.util.AbstractSet
import java.util.Collections
import java.util.EventListener
import java.util.Locale
import java.util.Random
import java.util.concurrent.Callable
import java.util.function.Consumer
import java.util.logging.Logger
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.test.Ignore
import kotlin.test.Test
import org.junit.Rule

@OptIn(ExperimentalKaviApi::class)
class TypeSpecJvmTest {
  private val tacosPackage = "com.squareup.tacos"

  @Rule @JvmField val compilation = CompilationRule()

  private fun getElement(`class`: Class<*>): TypeElement {
    return compilation.elements.getTypeElement(`class`.canonicalName)
  }

  private fun getElement(`class`: KClass<*>): TypeElement {
    return getElement(`class`.java)
  }

  @Test
  fun interestingTypes() {
    val listOfAny = List::class.asClassName().parameterizedBy(STAR)
    val listOfExtends =
      List::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Serializable::class))
    val listOfSuper =
      List::class.asClassName().parameterizedBy(WildcardTypeName.consumerOf(String::class))
    val taco =
      TypeSpec.classBuilder("Taco")
        .addProperty("star", listOfAny)
        .addProperty("outSerializable", listOfExtends)
        .addProperty("inString", listOfSuper)
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import kotlin.String
        |import kotlin.collections.List
        |
        |public class Taco {
        |  public val star: List<*>
        |
        |  public val outSerializable: List<out Serializable>
        |
        |  public val inString: List<in String>
        |}
        |"""
          .trimMargin()
      )
  }

  // https://github.com/square/kotlinpoet/issues/315
  @Test
  fun anonymousClassWithMultipleSuperTypes() {
    val superclass = ClassName("com.squareup.wire", "Message")
    val anonymousClass =
      TypeSpec.anonymousClassBuilder()
        .superclass(superclass)
        .addSuperinterface(Runnable::class)
        .addFunction(
          FunSpec.builder("run")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addCode("/* code snippets */\n")
            .build()
        )
        .build()
    val taco =
      TypeSpec.classBuilder("Taco")
        .addProperty(
          PropertySpec.builder("NAME", Runnable::class).initializer("%L", anonymousClass).build()
        )
        .build()

    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import com.squareup.wire.Message
        |import java.lang.Runnable
        |
        |public class Taco {
        |  public val NAME: Runnable = object : Message(), Runnable {
        |    public override fun run() {
        |      /* code snippets */
        |    }
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  /**
   * We had a bug where annotations were preventing us from doing the right thing when resolving
   * imports. https://github.com/square/javapoet/issues/422
   */
  @Test
  fun annotationsAndJavaLangTypes() {
    val freeRange = ClassName("javax.annotation", "FreeRange")
    val taco =
      TypeSpec.classBuilder("EthicalTaco")
        .addProperty(
          "meat",
          String::class.asClassName()
            .copy(annotations = listOf(AnnotationSpec.builder(freeRange).build())),
        )
        .build()

    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import javax.`annotation`.FreeRange
        |import kotlin.String
        |
        |public class EthicalTaco {
        |  public val meat: @FreeRange String
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun enumWithPrimaryConstructorAndMultipleInterfaces() {
    val roshambo =
      TypeSpec.enumBuilder("Roshambo")
        .addSuperinterface(Runnable::class)
        .addSuperinterface(Cloneable::class)
        .addEnumConstant(
          "SCISSORS",
          TypeSpec.anonymousClassBuilder()
            .addSuperclassConstructorParameter("%S", "peace sign")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("handPosition", String::class, KModifier.PRIVATE)
            .initializer("handPosition")
            .build()
        )
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("handPosition", String::class).build()
        )
        .build()
    assertThat(toString(roshambo))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Runnable
        |import kotlin.Cloneable
        |import kotlin.String
        |
        |public enum class Roshambo(
        |  private val handPosition: String,
        |) : Runnable,
        |    Cloneable {
        |  SCISSORS("peace sign"),
        |  ;
        |}
        |"""
          .trimMargin()
      )
  }

  /** https://github.com/square/javapoet/issues/253 */
  @Test
  fun enumWithAnnotatedValues() {
    val roshambo =
      TypeSpec.enumBuilder("Roshambo")
        .addModifiers(KModifier.PUBLIC)
        .addEnumConstant(
          "ROCK",
          TypeSpec.anonymousClassBuilder().addAnnotation(java.lang.Deprecated::class).build(),
        )
        .addEnumConstant("PAPER")
        .addEnumConstant("SCISSORS")
        .build()
    assertThat(toString(roshambo))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Deprecated
        |
        |public enum class Roshambo {
        |  @Deprecated
        |  ROCK,
        |  PAPER,
        |  SCISSORS,
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun funThrows() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addModifiers(KModifier.ABSTRACT)
        .addFunction(FunSpec.builder("throwOne").throws(IOException::class).build())
        .addFunction(
          FunSpec.builder("throwTwo")
            .throws(IOException::class.asClassName(), ClassName(tacosPackage, "SourCreamException"))
            .build()
        )
        .addFunction(
          FunSpec.builder("abstractThrow")
            .addModifiers(KModifier.ABSTRACT)
            .throws(IOException::class)
            .build()
        )
        .addFunction(
          FunSpec.builder("nativeThrow")
            .addModifiers(KModifier.EXTERNAL)
            .throws(IOException::class)
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.IOException
        |import kotlin.jvm.Throws
        |
        |public abstract class Taco {
        |  @Throws(IOException::class)
        |  public fun throwOne() {
        |  }
        |
        |  @Throws(
        |    IOException::class,
        |    SourCreamException::class,
        |  )
        |  public fun throwTwo() {
        |  }
        |
        |  @Throws(IOException::class)
        |  public abstract fun abstractThrow()
        |
        |  @Throws(IOException::class)
        |  public external fun nativeThrow()
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun classImplementsExtends() {
    val taco = ClassName(tacosPackage, "Taco")
    val food = ClassName("com.squareup.tacos", "Food")
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addModifiers(KModifier.ABSTRACT)
        .superclass(AbstractSet::class.asClassName().parameterizedBy(food))
        .addSuperinterface(Serializable::class)
        .addSuperinterface(Comparable::class.asClassName().parameterizedBy(taco))
        .build()
    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import java.util.AbstractSet
        |import kotlin.Comparable
        |
        |public abstract class Taco : AbstractSet<Food>(), Serializable, Comparable<Taco>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun classImplementsExtendsPrimaryConstructorNoParams() {
    val taco = ClassName(tacosPackage, "Taco")
    val food = ClassName("com.squareup.tacos", "Food")
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addModifiers(ABSTRACT)
        .superclass(AbstractSet::class.asClassName().parameterizedBy(food))
        .addSuperinterface(Serializable::class)
        .addSuperinterface(Comparable::class.asClassName().parameterizedBy(taco))
        .primaryConstructor(FunSpec.constructorBuilder().build())
        .build()
    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import java.util.AbstractSet
        |import kotlin.Comparable
        |
        |public abstract class Taco() : AbstractSet<Food>(), Serializable, Comparable<Taco>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun classImplementsExtendsPrimaryConstructorWithParams() {
    val taco = ClassName(tacosPackage, "Taco")
    val food = ClassName("com.squareup.tacos", "Food")
    val typeSpec =
      TypeSpec.classBuilder("Taco")
        .addModifiers(ABSTRACT)
        .superclass(AbstractSet::class.asClassName().parameterizedBy(food))
        .addSuperinterface(Serializable::class)
        .addSuperinterface(Comparable::class.asClassName().parameterizedBy(taco))
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("name", String::class).build()
        )
        .build()
    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import java.util.AbstractSet
        |import kotlin.Comparable
        |import kotlin.String
        |
        |public abstract class Taco(
        |  name: String,
        |) : AbstractSet<Food>(),
        |    Serializable,
        |    Comparable<Taco>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun classImplementsInnerClass() {
    val outer = ClassName(tacosPackage, "Outer")
    val inner = outer.nestedClass("Inner")
    val callable = Callable::class.asClassName()
    val typeSpec =
      TypeSpec.classBuilder("Outer")
        .superclass(callable.parameterizedBy(inner))
        .addType(TypeSpec.classBuilder("Inner").addModifiers(KModifier.INNER).build())
        .build()

    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.concurrent.Callable
        |
        |public class Outer : Callable<Outer.Inner>() {
        |  public inner class Inner
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun enumImplements() {
    val typeSpec =
      TypeSpec.enumBuilder("Food")
        .addSuperinterface(Serializable::class)
        .addSuperinterface(Cloneable::class)
        .addEnumConstant("LEAN_GROUND_BEEF")
        .addEnumConstant("SHREDDED_CHEESE")
        .build()
    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import kotlin.Cloneable
        |
        |public enum class Food : Serializable, Cloneable {
        |  LEAN_GROUND_BEEF,
        |  SHREDDED_CHEESE,
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun interfaceExtends() {
    val taco = ClassName(tacosPackage, "Taco")
    val typeSpec =
      TypeSpec.interfaceBuilder("Taco")
        .addSuperinterface(Serializable::class)
        .addSuperinterface(Comparable::class.asClassName().parameterizedBy(taco))
        .build()
    assertThat(toString(typeSpec))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import kotlin.Comparable
        |
        |public interface Taco : Serializable, Comparable<Taco>
        |"""
          .trimMargin()
      )
  }

  @Ignore
  @Test
  fun innerAnnotationInAnnotationDeclaration() {
    val bar =
      TypeSpec.annotationBuilder("Bar")
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder("value", java.lang.Deprecated::class).build())
            .build()
        )
        .addProperty(
          PropertySpec.builder("value", java.lang.Deprecated::class).initializer("value").build()
        )
        .build()

    assertThat(toString(bar))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Deprecated
        |
        |annotation class Bar() {
        |  fun value(): Deprecated default @Deprecated
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun intersectionType() {
    val typeVariable = TypeVariableName("T", Comparator::class, Serializable::class)
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunction(
          FunSpec.builder("getComparator")
            .addTypeVariable(typeVariable)
            .returns(typeVariable)
            .addStatement("return null")
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import java.util.Comparator
        |
        |public class Taco {
        |  public fun <T> getComparator(): T where T : Comparator, T : Serializable = null
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun kdoc() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addKdoc("A hard or soft tortilla, loosely folded and filled with whatever\n")
        .addKdoc("[random][%T] tex-mex stuff we could find in the pantry\n", Random::class)
        .addKdoc(CodeBlock.of("and some [%T] cheese.\n", String::class))
        .addProperty(
          PropertySpec.builder("soft", Boolean::class)
            .addKdoc("True for a soft flour tortilla; false for a crunchy corn tortilla.\n")
            .build()
        )
        .addFunction(
          FunSpec.builder("refold")
            .addKdoc(
              "Folds the back of this taco to reduce sauce leakage.\n" +
                "\n" +
                "For [%T#KOREAN], the front may also be folded.\n",
              Locale::class,
            )
            .addParameter("locale", Locale::class)
            .build()
        )
        .build()
    // Mentioning a type in KDoc will not cause an import to be added (java.util.Random here), but
    // the short name will be used if it's already imported (java.util.Locale here).
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.Locale
        |import kotlin.Boolean
        |
        |/**
        | * A hard or soft tortilla, loosely folded and filled with whatever
        | * [random][java.util.Random] tex-mex stuff we could find in the pantry
        | * and some [kotlin.String] cheese.
        | */
        |public class Taco {
        |  /**
        |   * True for a soft flour tortilla; false for a crunchy corn tortilla.
        |   */
        |  public val soft: Boolean
        |
        |  /**
        |   * Folds the back of this taco to reduce sauce leakage.
        |   *
        |   * For [Locale#KOREAN], the front may also be folded.
        |   */
        |  public fun refold(locale: Locale) {
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun kdocWithParameters() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addKdoc("A hard or soft tortilla, loosely folded and filled with whatever\n")
        .addKdoc("[random][%T] tex-mex stuff we could find in the pantry\n", Random::class)
        .addKdoc(CodeBlock.of("and some [%T] cheese.\n", String::class))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter(
              ParameterSpec.builder("temperature", Double::class)
                .addKdoc(
                  CodeBlock.of(
                    "%L",
                    """
                    |Taco temperature. Can be as cold as the famous ice tacos from
                    |the Andes, or hot with lava-like cheese from the depths of
                    |the Ninth Circle.
                    |"""
                      .trimMargin(),
                  )
                )
                .build()
            )
            .addParameter("soft", Boolean::class)
            .addParameter(
              ParameterSpec.builder("mild", Boolean::class)
                .addKdoc(CodeBlock.of("%L", "Whether the taco is mild (ew) or crunchy (ye).\n"))
                .build()
            )
            .addParameter("nodoc", Int::class)
            .build()
        )
        .addProperty(
          PropertySpec.builder("soft", Boolean::class)
            .addKdoc("True for a soft flour tortilla; false for a crunchy corn tortilla.\n")
            .initializer("soft")
            .build()
        )
        .addProperty(
          PropertySpec.builder("mild", Boolean::class)
            .addKdoc("No one likes mild tacos.")
            .initializer("mild")
            .build()
        )
        .addProperty(
          PropertySpec.builder("nodoc", Int::class, KModifier.PRIVATE).initializer("nodoc").build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.Boolean
        |import kotlin.Double
        |import kotlin.Int
        |
        |/**
        | * A hard or soft tortilla, loosely folded and filled with whatever
        | * [random][java.util.Random] tex-mex stuff we could find in the pantry
        | * and some [kotlin.String] cheese.
        | *
        | * @param temperature Taco temperature. Can be as cold as the famous ice tacos from
        | * the Andes, or hot with lava-like cheese from the depths of
        | * the Ninth Circle.
        | * @param mild Whether the taco is mild (ew) or crunchy (ye).
        | */
        |public class Taco(
        |  temperature: Double,
        |  /**
        |   * True for a soft flour tortilla; false for a crunchy corn tortilla.
        |   */
        |  public val soft: Boolean,
        |  /**
        |   * No one likes mild tacos.
        |   */
        |  public val mild: Boolean,
        |  private val nodoc: Int,
        |)
        |"""
          .trimMargin()
      )
  }

  @Test
  fun varargs() {
    val taqueria =
      TypeSpec.classBuilder("Taqueria")
        .addFunction(
          FunSpec.builder("prepare")
            .addParameter("workers", Int::class)
            .addParameter("jobs", Runnable::class.asClassName(), VARARG)
            .build()
        )
        .build()
    assertThat(toString(taqueria))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Runnable
        |import kotlin.Int
        |
        |public class Taqueria {
        |  public fun prepare(workers: Int, vararg jobs: Runnable) {
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun varargsNotLast() {
    val taqueria =
      TypeSpec.classBuilder("Taqueria")
        .addFunction(
          FunSpec.builder("prepare")
            .addParameter("workers", Int::class)
            .addParameter("jobs", Runnable::class.asClassName(), VARARG)
            .addParameter("start", Boolean::class.asClassName())
            .build()
        )
        .build()
    assertThat(toString(taqueria))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Runnable
        |import kotlin.Boolean
        |import kotlin.Int
        |
        |public class Taqueria {
        |  public fun prepare(
        |    workers: Int,
        |    vararg jobs: Runnable,
        |    start: Boolean,
        |  ) {
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun indexedElseIf() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunction(
          FunSpec.builder("choices")
            .beginControlFlow("if (%1L != null || %1L == %2L)", "taco", "otherTaco")
            .addStatement("%T.out.println(%S)", System::class, "only one taco? NOO!")
            .nextControlFlow("else if (%1L.%3L && %2L.%3L)", "taco", "otherTaco", "isSupreme()")
            .addStatement("%T.out.println(%S)", System::class, "taco heaven")
            .endControlFlow()
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.System
        |
        |public class Taco {
        |  public fun choices() {
        |    if (taco != null || taco == otherTaco) {
        |      System.out.println("only one taco? NOO!")
        |    } else if (taco.isSupreme() && otherTaco.isSupreme()) {
        |      System.out.println("taco heaven")
        |    }
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun elseIf() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunction(
          FunSpec.builder("choices")
            .beginControlFlow("if (5 < 4) ")
            .addStatement("%T.out.println(%S)", System::class, "wat")
            .nextControlFlow("else if (5 < 6)")
            .addStatement("%T.out.println(%S)", System::class, "hello")
            .endControlFlow()
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.System
        |
        |public class Taco {
        |  public fun choices() {
        |    if (5 < 4)  {
        |      System.out.println("wat")
        |    } else if (5 < 6) {
        |      System.out.println("hello")
        |    }
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun inlineIndent() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunction(
          FunSpec.builder("inlineIndent")
            .addCode("if (3 < 4) {\n⇥%T.out.println(%S);\n⇤}\n", System::class, "hello")
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.System
        |
        |public class Taco {
        |  public fun inlineIndent() {
        |    if (3 < 4) {
        |      System.out.println("hello");
        |    }
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun parameterToString() {
    val parameter =
      ParameterSpec.builder("taco", ClassName(tacosPackage, "Taco"))
        .addModifiers(KModifier.CROSSINLINE)
        .addAnnotation(ClassName("javax.annotation", "Nullable"))
        .build()
    assertThat(parameter.toString())
      .isEqualTo("@javax.`annotation`.Nullable crossinline taco: com.squareup.tacos.Taco")
  }

  @Test
  fun anonymousClassToString() {
    val type =
      TypeSpec.anonymousClassBuilder()
        .addSuperinterface(Runnable::class)
        .addFunction(
          FunSpec.builder("run").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE).build()
        )
        .build()
    assertThat(type.toString())
      .isEqualTo(
        """
        |object : java.lang.Runnable {
        |  public override fun run() {
        |  }
        |}
        """
          .trimMargin()
      )
  }

  private fun toString(typeSpec: TypeSpec): String {
    return FileSpec.get(tacosPackage, typeSpec).toString()
  }

  @Test
  fun multilineStatementWithAnonymousClass() {
    val stringComparator = Comparator::class.parameterizedBy(String::class)
    val listOfString = List::class.parameterizedBy(String::class)
    val prefixComparator =
      TypeSpec.anonymousClassBuilder()
        .addSuperinterface(stringComparator)
        .addFunction(
          FunSpec.builder("compare")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .returns(Int::class)
            .addParameter("a", String::class)
            .addParameter("b", String::class)
            .addComment("Prefix the strings and compare them")
            .addStatement("return a.substring(0, length)\n" + ".compareTo(b.substring(0, length))")
            .build()
        )
        .build()
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunction(
          FunSpec.builder("comparePrefix")
            .returns(stringComparator)
            .addParameter("length", Int::class)
            .addComment("Return a new comparator for the target length.")
            .addStatement("return %L", prefixComparator)
            .build()
        )
        .addFunction(
          FunSpec.builder("sortPrefix")
            .addParameter("list", listOfString)
            .addParameter("length", Int::class)
            .addStatement("%T.sort(\nlist,\n%L)", Collections::class, prefixComparator)
            .build()
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.Collections
        |import java.util.Comparator
        |import kotlin.Int
        |import kotlin.String
        |import kotlin.collections.List
        |
        |public class Taco {
        |  public fun comparePrefix(length: Int): Comparator<String> {
        |    // Return a new comparator for the target length.
        |    return object : Comparator<String> {
        |      public override fun compare(a: String, b: String): Int {
        |        // Prefix the strings and compare them
        |        return a.substring(0, length)
        |            .compareTo(b.substring(0, length))
        |      }
        |    }
        |  }
        |
        |  public fun sortPrefix(list: List<String>, length: Int) {
        |    Collections.sort(
        |        list,
        |        object : Comparator<String> {
        |          public override fun compare(a: String, b: String): Int {
        |            // Prefix the strings and compare them
        |            return a.substring(0, length)
        |                .compareTo(b.substring(0, length))
        |          }
        |        })
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleAnnotationAddition() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addAnnotations(
          listOf(
            AnnotationSpec.builder(SuppressWarnings::class).addMember("%S", "unchecked").build(),
            AnnotationSpec.builder(Deprecated::class).build(),
          )
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.Deprecated
        |import java.lang.SuppressWarnings
        |
        |@SuppressWarnings("unchecked")
        |@Deprecated
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multiplePropertyAddition() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addProperties(
          listOf(
            PropertySpec.builder("ANSWER", Int::class, KModifier.CONST).build(),
            PropertySpec.builder("price", BigDecimal::class, PRIVATE).build(),
          )
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.math.BigDecimal
        |import kotlin.Int
        |
        |public class Taco {
        |  public const val ANSWER: Int
        |
        |  private val price: BigDecimal
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleFunctionAddition() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addFunctions(
          listOf(
            FunSpec.builder("getAnswer")
              .addModifiers(PUBLIC)
              .returns(Int::class)
              .addStatement("return %L", 42)
              .build(),
            FunSpec.builder("getRandomQuantity")
              .addModifiers(PUBLIC)
              .returns(Int::class)
              .addKdoc("chosen by fair dice roll ;)\n")
              .addStatement("return %L", 4)
              .build(),
          )
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.Int
        |
        |public class Taco {
        |  public fun getAnswer(): Int = 42
        |
        |  /**
        |   * chosen by fair dice roll ;)
        |   */
        |  public fun getRandomQuantity(): Int = 4
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleSuperinterfaceAddition() {
    val taco =
      TypeSpec.classBuilder("Taco")
        .addSuperinterfaces(
          listOf(Serializable::class.asTypeName(), EventListener::class.asTypeName())
        )
        .build()
    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.io.Serializable
        |import java.util.EventListener
        |
        |public class Taco : Serializable, EventListener
        |"""
          .trimMargin()
      )
  }

  @Test
  fun typeFromTypeMirror() {
    val mirror = getElement(String::class).asType()
    assertThat(CodeBlock.of("%T", mirror).toString()).isEqualTo("java.lang.String")
  }

  @Test
  fun typeFromTypeElement() {
    val element = getElement(String::class)
    assertThat(CodeBlock.of("%T", element).toString()).isEqualTo("java.lang.String")
  }

  @Test
  fun generalToBuilderEqualityTest() {
    val originatingElement = FakeElement()
    val comprehensiveTaco =
      TypeSpec.classBuilder("Taco")
        .addKdoc("SuperTaco")
        .addAnnotation(SuppressWarnings::class)
        .addModifiers(DATA)
        .addTypeVariable(TypeVariableName.of("State", listOf(ANY), IN).copy(reified = true))
        .addType(TypeSpec.companionObjectBuilder().build())
        .addType(TypeSpec.classBuilder("InnerTaco").addModifiers(INNER).build())
        .primaryConstructor(FunSpec.constructorBuilder().build())
        .superclass(ClassName("texmexfood", "TortillaBased"))
        .addSuperclassConstructorParameter("true")
        .addProperty(PropertySpec.builder("meat", ClassName("texmexfood", "Meat")).build())
        .addFunction(FunSpec.builder("fold").build())
        .addSuperinterface(ClassName("texmexfood", "Consumable"))
        .addOriginatingElement(originatingElement)
        .build()

    val newTaco = comprehensiveTaco.toBuilder().build()
    assertThat(newTaco).isEqualTo(comprehensiveTaco)
    assertThat(newTaco.originatingElements).containsExactly(originatingElement)
  }

  @Test
  fun generalInterfaceBuilderEqualityTest() {
    val taco =
      TypeSpec.interfaceBuilder("Taco")
        .addProperty("isVegan", Boolean::class)
        .addSuperinterface(Runnable::class)
        .build()
    assertThat(taco.toBuilder().build()).isEqualTo(taco)
  }

  @Test
  fun generalAnonymousClassBuilderEqualityTest() {
    val anonObjectSpec =
      TypeSpec.anonymousClassBuilder()
        .addSuperinterface(Runnable::class)
        .addFunction(
          FunSpec.builder("run").addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE).build()
        )
        .build()
    assertThat(anonObjectSpec.toBuilder().build()).isEqualTo(anonObjectSpec)
  }

  @Test
  fun annotatedConstructor() {
    val injectAnnotation = ClassName("javax.inject", "Inject")
    val taco =
      TypeSpec.classBuilder("Taco")
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addAnnotation(AnnotationSpec.builder(injectAnnotation).build())
            .build()
        )
        .build()

    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import javax.inject.Inject
        |
        |public class Taco @Inject constructor()
        |"""
          .trimMargin()
      )
  }

  @Test
  fun annotatedInternalConstructor() {
    val injectAnnotation = ClassName("javax.inject", "Inject")
    val taco =
      TypeSpec.classBuilder("Taco")
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addAnnotation(AnnotationSpec.builder(injectAnnotation).build())
            .addModifiers(INTERNAL)
            .build()
        )
        .build()

    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import javax.inject.Inject
        |
        |public class Taco @Inject internal constructor()
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleAnnotationsInternalConstructor() {
    val injectAnnotation = ClassName("javax.inject", "Inject")
    val namedAnnotation = ClassName("javax.inject", "Named")
    val taco =
      TypeSpec.classBuilder("Taco")
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addAnnotation(AnnotationSpec.builder(injectAnnotation).build())
            .addAnnotation(AnnotationSpec.builder(namedAnnotation).build())
            .addModifiers(INTERNAL)
            .build()
        )
        .build()

    assertThat(toString(taco))
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import javax.inject.Inject
        |import javax.inject.Named
        |
        |public class Taco @Inject @Named internal constructor()
        |"""
          .trimMargin()
      )
  }

  @Test
  fun basicDelegateTest() {
    val type =
      TypeSpec.classBuilder("Guac")
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("somethingElse", String::class).build()
        )
        .addSuperinterface(
          Consumer::class.parameterizedBy(String::class),
          CodeBlock.of("({ println(it) })"),
        )
        .build()

    val expect =
      """
      |package com.squareup.tacos
      |
      |import java.util.function.Consumer
      |import kotlin.String
      |
      |public class Guac(
      |  somethingElse: String,
      |) : Consumer<String> by ({ println(it) })
      |"""
        .trimMargin()

    assertThat(toString(type)).isEqualTo(expect)
  }

  @Test
  fun testDelegateOnObject() {
    val type =
      TypeSpec.objectBuilder("Guac")
        .addSuperinterface(
          Consumer::class.parameterizedBy(String::class),
          CodeBlock.of("Consumer({ println(it) })"),
        )
        .build()

    val expect =
      """
      |package com.squareup.tacos
      |
      |import java.util.function.Consumer
      |import kotlin.String
      |
      |public object Guac : Consumer<String> by Consumer({ println(it) })
      |"""
        .trimMargin()

    assertThat(toString(type)).isEqualTo(expect)
  }

  @Test
  fun testMultipleDelegates() {
    val type =
      TypeSpec.classBuilder("StringToInteger")
        .primaryConstructor(FunSpec.constructorBuilder().build())
        .addSuperinterface(
          Function::class.parameterizedBy(String::class, Int::class),
          CodeBlock.of("Function ({ text -> text.toIntOrNull() ?: 0 })"),
        )
        .addSuperinterface(
          Runnable::class,
          CodeBlock.of("Runnable ({ %T.debug(\"Hello world\") })", Logger::class),
        )
        .build()

    val expect =
      """
      |package com.squareup.tacos
      |
      |import java.lang.Runnable
      |import java.util.logging.Logger
      |import kotlin.Function
      |import kotlin.Int
      |import kotlin.String
      |
      |public class StringToInteger() : Function<String, Int> by Function ({ text -> text.toIntOrNull() ?: 0 }),
      |    Runnable by Runnable ({ Logger.debug("Hello world") })
      |"""
        .trimMargin()

    assertThat(toString(type)).isEqualTo(expect)
  }

  // https://github.com/square/kotlinpoet/issues/2033
  @Test
  fun testDelegateOnAnonymousObject() {
    val type =
      TypeSpec.anonymousClassBuilder()
        .addSuperinterface(
          Consumer::class.parameterizedBy(String::class),
          CodeBlock.of("java.util.function.Consumer({ println(it) })"),
        )
        .build()

    val expect =
      """
      |object : java.util.function.Consumer<kotlin.String> by java.util.function.Consumer({ println(it) }) {
      |}
      """
        .trimMargin()

    assertThat(type.toString()).isEqualTo(expect)
  }

  // https://github.com/square/kotlinpoet/issues/2033
  @Test
  fun testMultipleDelegatesOnAnonymousObject() {
    val type =
      TypeSpec.anonymousClassBuilder()
        .addSuperinterface(
          Function::class.parameterizedBy(String::class, Int::class),
          CodeBlock.of("kotlin.Function ({ text -> text.toIntOrNull() ?: 0 })"),
        )
        .addSuperinterface(
          Runnable::class,
          CodeBlock.of("java.lang.Runnable ({ %T.debug(\"Hello world\") })", Logger::class),
        )
        .build()

    val expect =
      """
      |object : kotlin.Function<kotlin.String, kotlin.Int> by kotlin.Function ({ text -> text.toIntOrNull() ?: 0 }), java.lang.Runnable by java.lang.Runnable ({ java.util.logging.Logger.debug("Hello world") }) {
      |}
      """
        .trimMargin()

    assertThat(type.toString()).isEqualTo(expect)
  }

  @Test
  fun testNoSuchParameterDelegate() {
    assertFailure {
        TypeSpec.classBuilder("Taco")
          .primaryConstructor(
            FunSpec.constructorBuilder().addParameter("other", String::class).build()
          )
          .addSuperinterface(KFunction::class, "notOther")
          .build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("no such constructor parameter 'notOther' to delegate to for type 'Taco'")
  }

  @Test
  fun failAddParamDelegateWhenNullConstructor() {
    assertFailure {
        TypeSpec.classBuilder("Taco").addSuperinterface(Runnable::class, "etc").build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("delegating to constructor parameter requires not-null constructor")
  }

  @Test
  fun originatingElementsIncludesThoseOfNestedTypes() {
    val outerElement = FakeElement()
    val innerElement = FakeElement()
    val outer =
      TypeSpec.classBuilder("Outer")
        .addOriginatingElement(outerElement)
        .addType(TypeSpec.classBuilder("Inner").addOriginatingElement(innerElement).build())
        .build()
    assertThat(outer.originatingElements).containsExactly(outerElement, innerElement)
  }

  companion object {
    private const val donutsPackage = "com.squareup.donuts"
  }
}
