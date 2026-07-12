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
import io.github.kavikt.AnnotationSpec.UseSiteTarget.FILE
import io.github.kavikt.AnnotationSpec.UseSiteTarget.SET
import io.github.kavikt.MemberName.Companion.member
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.test.Test

class FileSpecTest {

  // kmp: this test was extracted: importStaticReadmeExample

  // kmp: this test was extracted: importStaticMixed

  @Test
  fun importTopLevel() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addImport("com.squareup.tacos.internal", "INGREDIENTS", "wrap")
        .addFunction(
          FunSpec.builder("prepareTacos")
            .returns(LIST.parameterizedBy(ClassName("com.squareup.tacos", "Taco")))
            .addCode("return wrap(INGREDIENTS)\n")
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import com.squareup.tacos.`internal`.INGREDIENTS
        |import com.squareup.tacos.`internal`.wrap
        |import kotlin.collections.List
        |
        |public fun prepareTacos(): List<Taco> = wrap(INGREDIENTS)
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: importStaticDynamic

  // kmp: this test was extracted: importStaticNone

  // kmp: this test was extracted: importStaticOnce

  // kmp: this test was extracted: importStaticTwice

  // kmp: this test was extracted: importStaticWildcardsForbidden

  @Test
  fun noImports() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(TypeSpec.classBuilder("Taco").build())
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun singleImport() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addProperty("madeFreshDate", ClassName("java.util", "Date"))
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.Date
        |
        |public class Taco {
        |  public val madeFreshDate: Date
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun singleImportEscapeKeywords() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addProperty("madeFreshDate", ClassName("com.squareup.is.fun.in", "Date"))
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import com.squareup.`is`.`fun`.`in`.Date
        |
        |public class Taco {
        |  public val madeFreshDate: Date
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun escapeSpacesInPackageName() {
    val file = FileSpec.builder("com.squareup.taco factory", "TacoFactory").build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.squareup.`taco factory`
        |
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingImports() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addProperty("madeFreshDate", ClassName("java.util", "Date"))
            .addProperty("madeFreshDatabaseDate", ClassName("java.sql", "Date"))
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.sql.Date as SqlDate
        |import java.util.Date as UtilDate
        |
        |public class Taco {
        |  public val madeFreshDate: UtilDate
        |
        |  public val madeFreshDatabaseDate: SqlDate
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingImportsEscapedWithoutBackticks() {
    val foo1Type = ClassName("com.example.generated.one", "\$Foo")
    val foo2Type = ClassName("com.example.generated.another", "\$Foo")

    val testFun =
      FunSpec.builder("testFun")
        .addCode(
          """
          val foo1 = %T()
          val foo2 = %T()
          """
            .trimIndent(),
          foo1Type,
          foo2Type,
        )
        .build()

    val testFile =
      FileSpec.builder("io.github.kavikt.test", "TestFile").addFunction(testFun).build()

    assertThat(testFile.toString())
      .isEqualTo(
        """
        |package io.github.kavikt.test
        |
        |import com.example.generated.another.`${'$'}Foo` as Another__Foo
        |import com.example.generated.one.`${'$'}Foo` as One__Foo
        |
        |public fun testFun() {
        |  val foo1 = One__Foo()
        |  val foo2 = Another__Foo()
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingImportsEscapeKeywords() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addProperty("madeFreshDate1", ClassName("com.squareup.is.fun.in", "Date"))
            .addProperty("madeFreshDate2", ClassName("com.squareup.do.val.var", "Date"))
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import com.squareup.`do`.`val`.`var`.Date as VarDate
        |import com.squareup.`is`.`fun`.`in`.Date as InDate
        |
        |public class Taco {
        |  public val madeFreshDate1: InDate
        |
        |  public val madeFreshDate2: VarDate
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun escapeSpacesInImports() {
    val tacoFactory = ClassName("com.squareup.taco factory", "TacoFactory")
    val file =
      FileSpec.builder("com.example", "TacoFactoryDemo")
        .addFunction(
          FunSpec.builder("main").addStatement("println(%T.produceTacos())", tacoFactory).build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.example
        |
        |import com.squareup.`taco factory`.TacoFactory
        |
        |public fun main() {
        |  println(TacoFactory.produceTacos())
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun escapeSpacesInAliasedImports() {
    val tacoFactory = ClassName("com.squareup.taco factory", "TacoFactory")
    val file =
      FileSpec.builder("com.example", "TacoFactoryDemo")
        .addAliasedImport(tacoFactory, "La Taqueria")
        .addFunction(
          FunSpec.builder("main").addStatement("println(%T.produceTacos())", tacoFactory).build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.example
        |
        |import com.squareup.`taco factory`.TacoFactory as `La Taqueria`
        |
        |public fun main() {
        |  println(`La Taqueria`.produceTacos())
        |}
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: aliasedImports

  @Test
  fun enumAliasedImport() {
    val timeUnit = ClassName("java.util.concurrent", "TimeUnit")
    val minsAlias = "MINS"
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addAliasedImport(timeUnit, "MINUTES", minsAlias)
        .addFunction(
          FunSpec.builder("sleepForFiveMins").addStatement("%T.MINUTES.sleep(5)", timeUnit).build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import java.util.concurrent.TimeUnit.MINUTES as MINS
        |
        |public fun sleepForFiveMins() {
        |  MINS.sleep(5)
        |}
        |"""
          .trimMargin()
      )
  }

  // https://github.com/square/kotlinpoet/issues/1696
  @Test
  fun aliasedImportInSamePackage() {
    val packageName = "com.mypackage"
    val className = ClassName(packageName, "StringKey")
    val source =
      FileSpec.builder(packageName, "K")
        .addAliasedImport(className, "S")
        .addType(
          TypeSpec.objectBuilder("K")
            .addProperty(
              PropertySpec.builder("test", className).initializer("%T(%L)", className, 0).build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.mypackage
        |
        |import com.mypackage.StringKey as S
        |
        |public object K {
        |  public val test: S = S(0)
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun aliasedImportClass() {
    val packageName = "com.mypackage"
    val className = ClassName(packageName, "Class")
    val source =
      FileSpec.builder(packageName, "K")
        .addAliasedImport(className, "C")
        .addFunction(
          FunSpec.builder("main").returns(className).addCode("return %T()", className).build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.mypackage
        |
        |import com.mypackage.Class as C
        |
        |public fun main(): C = C()
        |"""
          .trimMargin()
      )
  }

  @Test
  fun aliasedImportWithNestedClass() {
    val packageName = "com.mypackage"
    val className = ClassName(packageName, "Outer").nestedClass("Inner")
    val source =
      FileSpec.builder(packageName, "K")
        .addAliasedImport(className, "INNER")
        .addFunction(
          FunSpec.builder("main").returns(className).addCode("return %T()", className).build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.mypackage
        |
        |import com.mypackage.Outer.Inner as INNER
        |
        |public fun main(): INNER = INNER()
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingParentName() {
    val source =
      FileSpec.builder("com.squareup.tacos", "A")
        .addType(
          TypeSpec.classBuilder("A")
            .addType(
              TypeSpec.classBuilder("B")
                .addType(TypeSpec.classBuilder("Twin").build())
                .addType(
                  TypeSpec.classBuilder("C")
                    .addProperty("d", ClassName("com.squareup.tacos", "A", "Twin", "D"))
                    .build()
                )
                .build()
            )
            .addType(
              TypeSpec.classBuilder("Twin").addType(TypeSpec.classBuilder("D").build()).build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class A {
        |  public class B {
        |    public class Twin
        |
        |    public class C {
        |      public val d: A.Twin.D
        |    }
        |  }
        |
        |  public class Twin {
        |    public class D
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingChildName() {
    val source =
      FileSpec.builder("com.squareup.tacos", "A")
        .addType(
          TypeSpec.classBuilder("A")
            .addType(
              TypeSpec.classBuilder("B")
                .addType(
                  TypeSpec.classBuilder("C")
                    .addProperty("d", ClassName("com.squareup.tacos", "A", "Twin", "D"))
                    .addType(TypeSpec.classBuilder("Twin").build())
                    .build()
                )
                .build()
            )
            .addType(
              TypeSpec.classBuilder("Twin").addType(TypeSpec.classBuilder("D").build()).build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class A {
        |  public class B {
        |    public class C {
        |      public val d: A.Twin.D
        |
        |      public class Twin
        |    }
        |  }
        |
        |  public class Twin {
        |    public class D
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun conflictingNameOutOfScope() {
    val source =
      FileSpec.builder("com.squareup.tacos", "A")
        .addType(
          TypeSpec.classBuilder("A")
            .addType(
              TypeSpec.classBuilder("B")
                .addType(
                  TypeSpec.classBuilder("C")
                    .addProperty("d", ClassName("com.squareup.tacos", "A", "Twin", "D"))
                    .addType(
                      TypeSpec.classBuilder("Nested")
                        .addType(TypeSpec.classBuilder("Twin").build())
                        .build()
                    )
                    .build()
                )
                .build()
            )
            .addType(
              TypeSpec.classBuilder("Twin").addType(TypeSpec.classBuilder("D").build()).build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class A {
        |  public class B {
        |    public class C {
        |      public val d: Twin.D
        |
        |      public class Nested {
        |        public class Twin
        |      }
        |    }
        |  }
        |
        |  public class Twin {
        |    public class D
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun nestedClassAndSuperclassShareName() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .superclass(ClassName("com.squareup.wire", "Message"))
            .addType(
              TypeSpec.classBuilder("Builder")
                .superclass(ClassName("com.squareup.wire", "Message", "Builder"))
                .build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import com.squareup.wire.Message
        |
        |public class Taco : Message() {
        |  public class Builder : Message.Builder()
        |}
        |"""
          .trimMargin()
      )
  }

  /** https://github.com/square/javapoet/issues/366 */
  @Test
  fun annotationIsNestedClass() {
    val source =
      FileSpec.builder("com.squareup.tacos", "TestComponent")
        .addType(
          TypeSpec.classBuilder("TestComponent")
            .addAnnotation(ClassName("dagger", "Component"))
            .addType(
              TypeSpec.classBuilder("Builder")
                .addAnnotation(ClassName("dagger", "Component", "Builder"))
                .build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import dagger.Component
        |
        |@Component
        |public class TestComponent {
        |  @Component.Builder
        |  public class Builder
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun defaultPackage() {
    val source =
      FileSpec.builder("", "HelloWorld")
        .addType(
          TypeSpec.classBuilder("HelloWorld")
            .addFunction(
              FunSpec.builder("main")
                .addModifiers(KModifier.PUBLIC)
                .addParameter("args", ARRAY.parameterizedBy(STRING))
                .addCode("%T.out.println(%S);\n", ClassName("java.lang", "System"), "Hello World!")
                .build()
            )
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |import java.lang.System
        |import kotlin.Array
        |import kotlin.String
        |
        |public class HelloWorld {
        |  public fun main(args: Array<String>) {
        |    System.out.println("Hello World!");
        |  }
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun defaultPackageTypesAreImported() {
    val source =
      FileSpec.builder("hello", "World")
        .addType(TypeSpec.classBuilder("World").addSuperinterface(ClassName("", "Test")).build())
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package hello
        |
        |import Test
        |
        |public class World : Test
        |"""
          .trimMargin()
      )
  }

  @Test
  fun addCommentThroughCodeBlockHolderBuilder() {
    val builder =
      FileSpec.builder("com.squareup.tacos", "Taco").addType(TypeSpec.classBuilder("Taco").build())
    val holder: CodeBlockHolder.Builder<*> = builder
    holder.addComment("Generated %L. DO NOT EDIT!", "2015-01-13")
    assertThat(builder.build().toString())
      .isEqualTo(
        """
        |// Generated 2015-01-13. DO NOT EDIT!
        |package com.squareup.tacos
        |
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun topOfFileComment() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(TypeSpec.classBuilder("Taco").build())
        .addFileComment("Generated %L by Kavi. DO NOT EDIT!", "2015-01-13")
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |// Generated 2015-01-13 by Kavi. DO NOT EDIT!
        |package com.squareup.tacos
        |
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun emptyLinesInTopOfFileComment() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(TypeSpec.classBuilder("Taco").build())
        .addFileComment("\nGENERATED FILE:\n\nDO NOT EDIT!\n")
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |//
        |// GENERATED FILE:
        |//
        |// DO NOT EDIT!
        |//
        |package com.squareup.tacos
        |
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun packageClassConflictsWithNestedClass() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(
          TypeSpec.classBuilder("Taco")
            .addProperty("a", ClassName("com.squareup.tacos", "A"))
            .addType(TypeSpec.classBuilder("A").build())
            .build()
        )
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class Taco {
        |  public val a: com.squareup.tacos.A
        |
        |  public class A
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleTypesInOneFile() {
    val source =
      FileSpec.builder("com.squareup.tacos", "AB")
        .addType(TypeSpec.classBuilder("A").build())
        .addType(TypeSpec.classBuilder("B").build())
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class A
        |
        |public class B
        |"""
          .trimMargin()
      )
  }

  @Test
  fun simpleTypeAliases() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addTypeAlias(TypeAliasSpec.builder("Int8", BYTE).build())
        .addTypeAlias(TypeAliasSpec.builder("FileTable", MAP.parameterizedBy(STRING, INT)).build())
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |import kotlin.Byte
        |import kotlin.Int
        |import kotlin.String
        |import kotlin.collections.Map
        |
        |public typealias Int8 = Byte
        |
        |public typealias FileTable = Map<String, Int>
        |"""
          .trimMargin()
      )
  }

  @Test
  fun fileAnnotations() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addAnnotation(
          AnnotationSpec.builder(ClassName("kotlin.jvm", "JvmName"))
            .useSiteTarget(FILE)
            .addMember("%S", "TacoUtils")
            .build()
        )
        .addAnnotation(ClassName("kotlin.jvm", "JvmMultifileClass"))
        .build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |@file:JvmName("TacoUtils")
        |@file:JvmMultifileClass
        |
        |package com.squareup.tacos
        |
        |import kotlin.jvm.JvmMultifileClass
        |import kotlin.jvm.JvmName
        |
        |"""
          .trimMargin()
      )
  }

  @Test
  fun fileAnnotationMustHaveCorrectUseSiteTarget() {
    val builder = FileSpec.builder("com.squareup.tacos", "Taco")
    val annotation =
      AnnotationSpec.builder(ClassName("kotlin.jvm", "JvmName"))
        .useSiteTarget(SET)
        .addMember("value", "%S", "TacoUtils")
        .build()
    assertFailure { builder.addAnnotation(annotation) }
      .isInstanceOf<IllegalStateException>()
      .hasMessage("Use-site target SET not supported for file annotations.")
  }

  @Test
  fun escapeKeywordInPackageName() {
    val source = FileSpec.builder("com.squareup.is.fun.in", "California").build()
    assertThat(source.toString())
      .isEqualTo(
        """
        |package com.squareup.`is`.`fun`.`in`
        |
        |"""
          .trimMargin()
      )
  }

  @Test
  fun generalBuilderEqualityTest() {
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addAnnotation(ClassName("kotlin.jvm", "JvmMultifileClass"))
        .addFileComment("Generated 2015-01-13 by Kavi. DO NOT EDIT!")
        .addImport("com.squareup.tacos.internal", "INGREDIENTS")
        .addTypeAlias(TypeAliasSpec.builder("Int8", BYTE).build())
        .indent("  ")
        .addFunction(
          FunSpec.builder("defaultIngredients").addCode("println(INGREDIENTS)\n").build()
        )
        .build()

    assertThat(source.toBuilder().build()).isEqualTo(source)
  }

  @Test
  fun modifyAnnotations() {
    val jvmName = ClassName("kotlin.jvm", "JvmName")
    val builder =
      FileSpec.builder("com.taco", "Taco")
        .addAnnotation(
          AnnotationSpec.builder(jvmName)
            .useSiteTarget(FILE)
            .addMember("name = %S", "JvmTaco")
            .build()
        )

    val javaWord =
      AnnotationSpec.builder(jvmName).useSiteTarget(FILE).addMember("name = %S", "JavaTaco").build()
    builder.annotations.clear()
    builder.annotations.add(javaWord)

    assertThat(builder.build().annotations).containsExactly(javaWord)
  }

  @Test
  fun modifyImports() {
    val builder = FileSpec.builder("com.taco", "Taco").addImport("com.foo", "Foo")

    val currentImports = builder.imports
    builder.clearImports()
    builder
      .addImport("com.foo", "Foo2")
      .apply {
        for (current in currentImports) {
          addImport(current)
        }
      }
      .indent("")

    assertThat(builder.build().toString())
      .isEqualTo(
        """
        package com.taco

        import com.foo.Foo
        import com.foo.Foo2


        """
          .trimIndent()
      )
  }

  @Test
  fun memberNameImports() {
    val getValue = MemberName("androidx.compose.runtime", "getValue")
    val mutableStateOf = MemberName("androidx.compose.runtime", "mutableStateOf")
    val file =
      FileSpec.builder("com.taco", "Taco")
        .addImport(getValue)
        .addProperty(
          PropertySpec.builder("name", STRING)
            .delegate("%M<%T>(%S)", mutableStateOf, STRING, "Jake")
            .build()
        )
        .build()

    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.taco
        |
        |import androidx.compose.runtime.getValue
        |import androidx.compose.runtime.mutableStateOf
        |import kotlin.String
        |
        |public val name: String by mutableStateOf<String>("Jake")
        |"""
          .trimMargin()
      )
  }

  @Test
  fun modifyMembers() {
    val builder =
      FileSpec.builder("com.taco", "Taco")
        .addFunction(FunSpec.builder("aFunction").build())
        .addProperty(PropertySpec.builder("aProperty", INT).initializer("1").build())
        .addTypeAlias(TypeAliasSpec.builder("ATypeAlias", INT).build())
        .addType(TypeSpec.classBuilder("AClass").build())

    builder.members.removeAll { it !is TypeSpec }

    check(builder.build().members.all { it is TypeSpec })
  }

  @Test
  fun clearComment() {
    val builder =
      FileSpec.builder("com.taco", "Taco")
        .addFunction(FunSpec.builder("aFunction").build())
        .addFileComment("Hello!")

    builder.clearComment().addFileComment("Goodbye!")

    assertThat(builder.build().comment.toString()).isEqualTo("Goodbye!")
  }

  // https://github.com/square/kotlinpoet/issues/480
  @Test
  fun defaultPackageMemberImport() {
    val bigInteger = ClassName.bestGuess("bigInt.BigInteger")
    val spec =
      FileSpec.builder("testsrc", "Test")
        .addImport("", "bigInt")
        .addFunction(
          FunSpec.builder("add5ToInput")
            .addParameter("input", INT)
            .returns(bigInteger)
            .addCode(
              """
              |val inputBigInt = bigInt(input)
              |return inputBigInt.add(5)
              |"""
                .trimMargin()
            )
            .build()
        )
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |package testsrc
        |
        |import bigInt
        |import bigInt.BigInteger
        |import kotlin.Int
        |
        |public fun add5ToInput(input: Int): BigInteger {
        |  val inputBigInt = bigInt(input)
        |  return inputBigInt.add(5)
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun longFilePackageName() {
    val spec =
      FileSpec.builder(
          "com.squareup.taco.enchilada.quesadillas.tamales.burritos.super.burritos.trying.to.get.a.really.large.packagename",
          "Test",
        )
        .addFunction(FunSpec.builder("foo").build())
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |package com.squareup.taco.enchilada.quesadillas.tamales.burritos.`super`.burritos.trying.to.`get`.a.really.large.packagename
        |
        |public fun foo() {
        |}
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importLongPackageName() {
    val spec =
      FileSpec.builder("testsrc", "Test")
        .addImport(
          "a.really.veryveryveryveryveryveryvery.long.pkgname.that.will.definitely.cause.a.wrap.duetoitslength",
          "MyClass",
        )
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |package testsrc
        |
        |import a.really.veryveryveryveryveryveryvery.long.pkgname.that.will.definitely.cause.a.wrap.duetoitslength.MyClass
        |
        |"""
          .trimMargin()
      )
  }

  @Test
  fun importAliasedLongPackageName() {
    val spec =
      FileSpec.builder("testsrc", "Test")
        .addAliasedImport(
          ClassName(
            "a.really.veryveryveryveryveryveryvery.long.pkgname.that.will.definitely.cause.a.wrap.duetoitslength",
            "MyClass",
          ),
          "MyClassAlias",
        )
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |package testsrc
        |
        |import a.really.veryveryveryveryveryveryvery.long.pkgname.that.will.definitely.cause.a.wrap.duetoitslength.MyClass as MyClassAlias
        |
        |"""
          .trimMargin()
      )
  }

  @Test
  fun longComment() {
    val file =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addFileComment(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
            "eiusmod tempor incididunt ut labore et dolore magna aliqua."
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |// Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
        |package com.squareup.tacos
        |
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: longCommentWithTypes

  @Test
  fun simpleScriptTest() {
    val spec =
      FileSpec.scriptBuilder("Taco")
        .addProperty(PropertySpec.builder("prop", STRING).initializer("\"hi\"").build())
        .addCode("\n")
        .addStatement("println(%S)", "hello!")
        .addCode("\n")
        .addFunction(FunSpec.builder("localFun").build())
        .addCode("\n")
        .addType(TypeSpec.classBuilder("Yay").build())
        .addCode("\n")
        .addStatement("val yayInstance = Yay()")
        .build()
    assertThat(spec.toString())
      .isEqualTo(
        """
        |import kotlin.String
        |
        |val prop: String = "hi"
        |
        |println("hello!")
        |
        |fun localFun() {
        |}
        |
        |public class Yay
        |
        |val yayInstance = Yay()
        |"""
          .trimMargin()
      )
  }

  // kmp: this test was extracted: defaultImports

  @Test
  fun classNameFactory() {
    val className = ClassName("com.example", "Example")
    val spec = FileSpec.builder(className).build()
    assertThat(spec.packageName).isEqualTo(className.packageName)
    assertThat(spec.name).isEqualTo(className.simpleName)
  }

  @Test
  fun classNameFactoryIllegalArgumentExceptionOnNestedType() {
    val className = ClassName("com.example", "Example", "Nested")
    assertFailure { FileSpec.builder(className) }.isInstanceOf<IllegalArgumentException>()
  }

  @Test
  fun memberNameFactory() {
    val memberName = MemberName("com.example", "Example")
    val spec = FileSpec.builder(memberName).build()
    assertThat(spec.packageName).isEqualTo(memberName.packageName)
    assertThat(spec.name).isEqualTo(memberName.simpleName)
  }

  @Test
  fun topLevelPropertyWithControlFlow() {
    val spec =
      FileSpec.builder("com.example.foo", "Test")
        .addProperty(
          PropertySpec.builder("MyProperty", ClassName("java.lang", "String"))
            .initializer(
              CodeBlock.builder()
                .beginControlFlow("if (1 + 1 == 2)")
                .addStatement("Expected")
                .nextControlFlow("else")
                .addStatement("Unexpected")
                .endControlFlow()
                .build()
            )
            .build()
        )
        .build()

    assertThat(spec.toString())
      .isEqualTo(
        """
        |package com.example.foo
        |
        |import java.lang.String
        |
        |public val MyProperty: String = if (1 + 1 == 2) {
        |  Expected
        |} else {
        |  Unexpected
        |}
        |"""
          .trimMargin()
      )
  }

  // https://github.com/square/kotlinpoet/issues/2216
  @Test
  fun typeNameImportedViaMemberImportRendersCorrectly() {
    val type = ClassName("com.example", "AnEnum")
    val block = CodeBlock.of("var field: %T = null", type.copy(nullable = true))

    val file =
      FileSpec.builder("com.example", "Test")
        .addType(
          TypeSpec.classBuilder("Test")
            .addProperty(
              PropertySpec.builder("field", type).initializer("%M", type.member("DEFAULT")).build()
            )
            .addFunction(FunSpec.builder("test").returns(UNIT).addCode(block).build())
            .build()
        )
        .build()
    assertThat(file.toString())
      .isEqualTo(
        """
        |package com.example
        |
        |import com.example.AnEnum.DEFAULT
        |
        |public class Test {
        |  public val `field`: AnEnum = DEFAULT
        |
        |  public fun test() {
        |    var field: AnEnum? = null
        |  }
        |}
        |"""
          .trimMargin()
      )
  }
}
