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
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.github.kavikt.KModifier.VARARG
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.function.Function
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") // Explicitly testing Java classes.
class FileSpecJvmTest {
  @Test
  fun importStaticReadmeExample() {
    val hoverboard = ClassName("com.mattel", "Hoverboard")
    val namedBoards = ClassName("com.mattel", "Hoverboard", "Boards")
    val list = List::class.asClassName()
    val arrayList = ClassName("java.util", "ArrayList").parameterizedBy(hoverboard)
    val listOfHoverboards = list.parameterizedBy(hoverboard)
    val beyond =
      FunSpec.builder("beyond")
        .returns(listOfHoverboards)
        .addStatement("val result = %T()", arrayList)
        .addStatement("result.add(%T.createNimbus(2000))", hoverboard)
        .addStatement("result.add(%T.createNimbus(\"2001\"))", hoverboard)
        .addStatement("result.add(%T.createNimbus(%T.THUNDERBOLT))", hoverboard, namedBoards)
        .addStatement("%T.sort(result)", Collections::class)
        .addStatement("return if (result.isEmpty()) %T.emptyList() else result", Collections::class)
        .build()
    val hello = TypeSpec.classBuilder("HelloWorld").addFunction(beyond).build()
    val source =
      FileSpec.builder("com.example.helloworld", "HelloWorld")
        .addType(hello)
        .addImport(hoverboard, "createNimbus")
        .addImport(namedBoards, "THUNDERBOLT")
        .addImport(Collections::class, "sort", "emptyList")
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.example.helloworld
        |
        |import com.mattel.Hoverboard
        |import com.mattel.Hoverboard.Boards.THUNDERBOLT
        |import com.mattel.Hoverboard.createNimbus
        |import java.util.ArrayList
        |import java.util.Collections.emptyList
        |import java.util.Collections.sort
        |import kotlin.collections.List
        |
        |public class HelloWorld {
        |  public fun beyond(): List<Hoverboard> {
        |    val result = ArrayList<Hoverboard>()
        |    result.add(createNimbus(2000))
        |    result.add(createNimbus("2001"))
        |    result.add(createNimbus(THUNDERBOLT))
        |    sort(result)
        |    return if (result.isEmpty()) emptyList() else result
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importStaticMixed() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addInitializerBlock(
              CodeBlock.builder()
                .addStatement("assert %1T.valueOf(\"BLOCKED\") == %1T.BLOCKED", Thread.State::class)
                .addStatement("%T.gc()", System::class)
                .addStatement("%1T.out.println(%1T.nanoTime())", System::class)
                .build()
            )
            .addFunction(
              FunSpec.constructorBuilder()
                .addParameter("states", Thread.State::class.asClassName(), VARARG)
                .build()
            )
            .build()
        )
        .addImport(Thread.State.BLOCKED)
        .addImport(System::class, "gc", "out", "nanoTime")
        .addImport(Thread.State::class, "valueOf")
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.System.`out`
        |import java.lang.System.gc
        |import java.lang.System.nanoTime
        |import java.lang.Thread
        |import java.lang.Thread.State.BLOCKED
        |import java.lang.Thread.State.valueOf
        |
        |public class Taco {
        |  init {
        |    assert valueOf("BLOCKED") == BLOCKED
        |    gc()
        |    out.println(nanoTime())
        |  }
        |
        |  public constructor(vararg states: Thread.State)
        |}
        |"""
          .trimMargin()
      )
  }

  @Ignore("addImport doesn't support members with %L")
  @Test
  fun importStaticDynamic() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addFunction(
              FunSpec.builder("main")
                .addStatement("%T.%L.println(%S)", System::class, "out", "hello")
                .build()
            )
            .build()
        )
        .addImport(System::class, "out")
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos;
        |
        |import static java.lang.System.out;
        |
        |class Taco {
        |  void main() {
        |    out.println("hello");
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importStaticNone() {
    val source = FileSpec.builder("readme", "Util").addType(importStaticTypeSpec("Util")).build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package readme
        |
        |import java.lang.System
        |import java.util.concurrent.TimeUnit
        |import kotlin.Long
        |
        |public class Util {
        |  public fun minutesToSeconds(minutes: Long): Long {
        |    System.gc()
        |    return TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES)
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importStaticOnce() {
    val source =
      FileSpec.builder("readme", "Util")
        .addType(importStaticTypeSpec("Util"))
        .addImport(TimeUnit.SECONDS)
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package readme
        |
        |import java.lang.System
        |import java.util.concurrent.TimeUnit
        |import java.util.concurrent.TimeUnit.SECONDS
        |import kotlin.Long
        |
        |public class Util {
        |  public fun minutesToSeconds(minutes: Long): Long {
        |    System.gc()
        |    return SECONDS.convert(minutes, TimeUnit.MINUTES)
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importStaticTwice() {
    val source =
      FileSpec.builder("readme", "Util")
        .addType(importStaticTypeSpec("Util"))
        .addImport(TimeUnit.SECONDS)
        .addImport(TimeUnit.MINUTES)
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package readme
        |
        |import java.lang.System
        |import java.util.concurrent.TimeUnit.MINUTES
        |import java.util.concurrent.TimeUnit.SECONDS
        |import kotlin.Long
        |
        |public class Util {
        |  public fun minutesToSeconds(minutes: Long): Long {
        |    System.gc()
        |    return SECONDS.convert(minutes, MINUTES)
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importStaticWildcardsForbidden() {
    assertFailure {
        FileSpec.builder("readme", "Util")
          .addType(importStaticTypeSpec("Util"))
          .addImport(TimeUnit::class, "*")
      }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Wildcard imports are not allowed")
  }

  private fun importStaticTypeSpec(name: String): TypeSpec {
    val funSpec =
      FunSpec.builder("minutesToSeconds")
        .addModifiers(KModifier.PUBLIC)
        .returns(Long::class)
        .addParameter("minutes", Long::class)
        .addStatement("%T.gc()", System::class)
        .addStatement("return %1T.SECONDS.convert(minutes, %1T.MINUTES)", TimeUnit::class)
        .build()
    return TypeSpec.classBuilder(name).addFunction(funSpec).build()
  }

  @Test
  fun aliasedImports() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addAliasedImport(java.lang.String::class.java, "JString")
        .addAliasedImport(String::class, "KString")
        .addProperty(
          PropertySpec.builder("a", java.lang.String::class.java)
            .initializer("%T(%S)", java.lang.String::class.java, "a")
            .build()
        )
        .addProperty(PropertySpec.builder("b", String::class).initializer("%S", "b").build())
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.lang.String as JString
        |import kotlin.String as KString
        |
        |public val a: JString = JString("a")
        |
        |public val b: KString = "b"
        |"""
          .trimMargin()
      )
  }

  class WackyKey

  class OhNoThisDoesNotCompile

  @Test
  fun longCommentWithTypes() {
    @Suppress("REDUNDANT_PROJECTION")
    val someLongParameterizedTypeName =
      typeNameOf<List<Map<in String, Collection<Map<WackyKey, out OhNoThisDoesNotCompile>>>>>()
    val param = ParameterSpec.builder("foo", someLongParameterizedTypeName).build()
    val someLongLambdaTypeName =
      LambdaTypeName.get(STRING, listOf(param), STRING).copy(suspending = true)
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addFunction(
          FunSpec.builder("f1")
            .addComment(
              "this is a long line with a possibly long parameterized type with annotation: %T",
              someLongParameterizedTypeName,
            )
            .build()
        )
        .addFunction(
          FunSpec.builder("f2")
            .addComment(
              "this is a very very very very very very very very very very long line with a very long lambda type: %T",
              someLongLambdaTypeName,
            )
            .build()
        )
        .build()

    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import io.github.kavikt.FileSpecJvmTest
        |import kotlin.String
        |import kotlin.collections.Collection
        |import kotlin.collections.List
        |import kotlin.collections.Map
        |
        |public fun f1() {
        |  // this is a long line with a possibly long parameterized type with annotation: List<Map<in String, Collection<Map<FileSpecJvmTest.WackyKey, out FileSpecJvmTest.OhNoThisDoesNotCompile>>>>
        |}
        |
        |public fun f2() {
        |  // this is a very very very very very very very very very very long line with a very long lambda type: suspend String.(foo: List<Map<in String, Collection<Map<FileSpecJvmTest.WackyKey, out FileSpecJvmTest.OhNoThisDoesNotCompile>>>>) -> String
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun defaultImports() {
    val spec =
      FileSpec.scriptBuilder("Taco")
        .addProperty(
          PropertySpec.builder("prop0", STRING.copy(nullable = true)).initializer("null").build()
        )
        .addProperty(
          PropertySpec.builder("prop1", INT.copy(nullable = true)).initializer("null").build()
        )
        .addProperty(
          PropertySpec.builder("prop2", typeNameOf<Map<String, Any>?>()).initializer("null").build()
        )
        .addProperty(
          PropertySpec.builder("prop3", typeNameOf<Callable<String>?>()).initializer("null").build()
        )
        .addProperty(
          PropertySpec.builder("prop4", typeNameOf<Function<Int, Int>?>())
            .initializer("null")
            .build()
        )
        .addKotlinDefaultImports()
        .addDefaultPackageImport("java.util.function")
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |import java.util.concurrent.Callable
        |
        |val prop0: String? = null
        |val prop1: Int? = null
        |val prop2: Map<String, Any>? = null
        |val prop3: @FunctionalInterface Callable<String>? = null
        |val prop4: @FunctionalInterface Function<Int, Int>? = null
        |"""
          .trimMargin()
      )
  }
}
