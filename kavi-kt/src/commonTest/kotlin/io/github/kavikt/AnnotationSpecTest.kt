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
import assertk.assertions.isTrue
import io.github.kavikt.KModifier.OVERRIDE
import io.github.kavikt.ParameterizedTypeName.Companion.parameterizedBy
import io.github.kavikt.ParameterizedTypeName.Companion.plusParameter
import kotlin.test.Test

class AnnotationSpecTest {

  @Test
  fun equalsAndHashCode() {
    val annotationC = ClassName("io.github.kavikt", "AnnotationC")
    var a = AnnotationSpec.builder(annotationC).build()
    var b = AnnotationSpec.builder(annotationC).build()
    assertThat(a == b).isTrue()
    assertThat(a.hashCode()).isEqualTo(b.hashCode())
    a = AnnotationSpec.builder(annotationC).addMember("value", "%S", "123").build()
    b = AnnotationSpec.builder(annotationC).addMember("value", "%S", "123").build()
    assertThat(a == b).isTrue()
    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }

  // kmp: this test was extracted: defaultAnnotation

  // kmp: this test was extracted: defaultAnnotationWithImport

  // kmp: this test was extracted: emptyArray

  // kmp: this test was extracted: reflectAnnotation

  // kmp: this test was extracted: reflectAnnotationWithDefaults

  @Test
  fun useSiteTarget() {
    val annotationA = ClassName("io.github.kavikt", "AnnotationSpecTest", "AnnotationA")
    val builder = AnnotationSpec.builder(annotationA)
    assertThat(builder.build().toString())
      .isEqualTo("" + "@io.github.kavikt.AnnotationSpecTest.AnnotationA")
    builder.useSiteTarget(AnnotationSpec.UseSiteTarget.FIELD)
    assertThat(builder.build().toString())
      .isEqualTo("" + "@field:io.github.kavikt.AnnotationSpecTest.AnnotationA")
    builder.useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
    assertThat(builder.build().toString())
      .isEqualTo("" + "@get:io.github.kavikt.AnnotationSpecTest.AnnotationA")
    builder.useSiteTarget(null)
    assertThat(builder.build().toString())
      .isEqualTo("" + "@io.github.kavikt.AnnotationSpecTest.AnnotationA")
  }

  @Test
  fun deprecatedTest() {
    val annotation =
      AnnotationSpec.builder(ClassName("kotlin", "Deprecated"))
        .addMember("%S", "Nope")
        .addMember("%T(%S)", ClassName("kotlin", "ReplaceWith"), "Yep")
        .build()

    assertThat(annotation.toString())
      .isEqualTo("" + "@kotlin.Deprecated(\"Nope\", kotlin.ReplaceWith(\"Yep\"))")
  }

  @Test
  fun modifyMembers() {
    val builder =
      AnnotationSpec.builder(ClassName("kotlin", "Deprecated"))
        .addMember("%S", "Nope")
        .addMember("%T(%S)", ClassName("kotlin", "ReplaceWith"), "Yep")

    builder.members.removeAt(1)
    builder.members.add(CodeBlock.of("%T(%S)", ClassName("kotlin", "ReplaceWith"), "Nope"))

    assertThat(builder.build().toString())
      .isEqualTo("" + "@kotlin.Deprecated(\"Nope\", kotlin.ReplaceWith(\"Nope\"))")
  }

  @Test
  fun annotationStringsAreConstant() {
    val text = "This is a long string with a newline\nin the middle."
    val builder = AnnotationSpec.builder(ClassName("kotlin", "Deprecated")).addMember("%S", text)

    assertThat(builder.build().toString())
      .isEqualTo(
        "" + "@kotlin.Deprecated(\"This is a long string with a newline\\nin the middle.\")"
      )
  }

  @Test
  fun literalAnnotation() {
    val annotationSpec =
      AnnotationSpec.builder(ClassName("kotlin", "Suppress")).addMember("%S", "Things").build()

    val file =
      FileSpec.builder("test", "Test")
        .addFunction(
          FunSpec.builder("test")
            .addStatement("%L", annotationSpec)
            .addStatement("val annotatedString = %S", "AnnotatedString")
            .build()
        )
        .build()
    assertThat(file.toString().trim())
      .isEqualTo(
        """
        |package test
        |
        |import kotlin.Suppress
        |
        |public fun test() {
        |  @Suppress("Things")
        |  val annotatedString = "AnnotatedString"
        |}
        """
          .trimMargin()
      )
  }

  @Test
  fun functionOnlyLiteralAnnotation() {
    val annotation =
      AnnotationSpec.builder(ClassName.bestGuess("Suppress"))
        .addMember("%S", "UNCHECKED_CAST")
        .build()
    val funSpec = FunSpec.builder("operation").addStatement("%L", annotation).build()

    assertThat(funSpec.toString().trim())
      .isEqualTo(
        """
        |public fun operation() {
        |  @Suppress("UNCHECKED_CAST")
        |}
        """
          .trimMargin()
      )
  }

  // kmp: this test was extracted: getOnVarargMirrorShouldNameValueArg

  // kmp: this test was extracted: getOnVarargAnnotationShouldNameValueArg

  @Test
  fun annotationsWithTypeParameters() {
    // Example from https://kotlinlang.org/docs/tutorials/android-plugin.html
    val externalClass = ClassName("com.squareup.parceler", "ExternalClass")
    val externalClassSpec =
      TypeSpec.classBuilder(externalClass)
        .addProperty(PropertySpec.builder("value", INT).initializer("value").build())
        .primaryConstructor(FunSpec.constructorBuilder().addParameter("value", INT).build())
        .build()
    val externalClassParceler = ClassName("com.squareup.parceler", "ExternalClassParceler")
    val parcel = ClassName("com.squareup.parceler", "Parcel")
    val externalClassParcelerSpec =
      TypeSpec.objectBuilder(externalClassParceler)
        .addSuperinterface(
          ClassName("com.squareup.parceler", "Parceler").parameterizedBy(externalClass)
        )
        .addFunction(
          FunSpec.builder("create")
            .addModifiers(OVERRIDE)
            .addParameter("parcel", parcel)
            .returns(externalClass)
            .addStatement("return %T(parcel.readInt())", externalClass)
            .build()
        )
        .addFunction(
          FunSpec.builder("write")
            .addModifiers(OVERRIDE)
            .receiver(externalClass)
            .addParameter("parcel", parcel)
            .addParameter("flags", INT)
            .addStatement("parcel.writeInt(value)")
            .build()
        )
        .build()
    val parcelize = ClassName("com.squareup.parceler", "Parcelize")
    val typeParceler = ClassName("com.squareup.parceler", "TypeParceler")
    val typeParcelerAnnotation =
      AnnotationSpec.builder(
          typeParceler.plusParameter(externalClass).plusParameter(externalClassParceler)
        )
        .build()
    val classLocalParceler =
      TypeSpec.classBuilder("MyClass")
        .addAnnotation(parcelize)
        .addAnnotation(typeParcelerAnnotation)
        .addProperty(
          PropertySpec.builder("external", externalClass).initializer("external").build()
        )
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("external", externalClass).build()
        )
        .build()
    val propertyLocalParceler =
      TypeSpec.classBuilder("MyClass")
        .addAnnotation(parcelize)
        .addProperty(
          PropertySpec.builder("external", externalClass)
            .addAnnotation(typeParcelerAnnotation)
            .initializer("external")
            .build()
        )
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("external", externalClass).build()
        )
        .build()
    val writeWith = ClassName("com.squareup.parceler", "WriteWith")
    val writeWithExternalClass =
      externalClass.copy(
        annotations =
          listOf(AnnotationSpec.builder(writeWith.plusParameter(externalClassParceler)).build())
      )
    val typeLocalParceler =
      TypeSpec.classBuilder("MyClass")
        .addAnnotation(parcelize)
        .addProperty(
          PropertySpec.builder("external", writeWithExternalClass).initializer("external").build()
        )
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("external", writeWithExternalClass).build()
        )
        .build()
    val file =
      FileSpec.builder("com.squareup.parceler", "Test")
        .addType(externalClassSpec)
        .addType(externalClassParcelerSpec)
        .addType(classLocalParceler)
        .addType(propertyLocalParceler)
        .addType(typeLocalParceler)
        .build()
    // language=kotlin
    assertThat(file.toString())
      .isEqualTo(
        """
        package com.squareup.parceler

        import kotlin.Int

        public class ExternalClass(
          public val `value`: Int,
        )

        public object ExternalClassParceler : Parceler<ExternalClass> {
          override fun create(parcel: Parcel): ExternalClass = ExternalClass(parcel.readInt())

          override fun ExternalClass.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(value)
          }
        }

        @Parcelize
        @TypeParceler<ExternalClass, ExternalClassParceler>
        public class MyClass(
          public val `external`: ExternalClass,
        )

        @Parcelize
        public class MyClass(
          @TypeParceler<ExternalClass, ExternalClassParceler>
          public val `external`: ExternalClass,
        )

        @Parcelize
        public class MyClass(
          public val `external`: @WriteWith<ExternalClassParceler> ExternalClass,
        )

        """
          .trimIndent()
      )
  }
}
