/*
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
@file:JvmName("AnnotationSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Array
import java.util.Objects
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleAnnotationValueVisitor8

/**
 * Creates a [CodeBlock] with parameter `format` depending on the given `value` object. Handles a
 * number of special cases, such as appending "f" to `Float` values, and uses `%L` for other types.
 */
internal fun memberForValue(value: Any) =
  when (value) {
    is Annotation -> CodeBlock.of("%L", value.toAnnotationSpec(includeDefaultValues = false))
    is Class<*> -> CodeBlock.of("%T::class", value)
    is Enum<*> -> CodeBlock.of("%T.%L", value.javaClass, value.name)
    is String -> CodeBlock.of("%S", value)
    is Float -> CodeBlock.of("%Lf", value)
    is Char -> CodeBlock.of("'%L'", characterLiteralWithoutSingleQuotes(value))
    else -> CodeBlock.of("%L", value)
  }

/** Annotation value visitor adding members to the given builder instance. */
@OptIn(DelicateKaviApi::class)
private class Visitor(val builder: CodeBlock.Builder) :
  SimpleAnnotationValueVisitor8<CodeBlock.Builder, String>(builder) {

  override fun defaultAction(o: Any, name: String) = builder.add(memberForValue(o))

  override fun visitAnnotation(a: AnnotationMirror, name: String) =
    builder.add("%L", a.toAnnotationSpec())

  override fun visitEnumConstant(c: VariableElement, name: String) =
    builder.add("%T.%L", c.asType().asTypeName(), c.simpleName)

  override fun visitType(t: TypeMirror, name: String) = builder.add("%T::class", t.asTypeName())

  override fun visitArray(values: List<AnnotationValue>, name: String): CodeBlock.Builder {
    builder.add("arrayOf(⇥⇥")
    values.forEachIndexed { index, value ->
      if (index > 0) builder.add(", ")
      value.accept(this, name)
    }
    builder.add("⇤⇤)")
    return builder
  }
}

@OptIn(DelicateKaviApi::class)
internal fun Annotation.toAnnotationSpec(includeDefaultValues: Boolean): AnnotationSpec {
  try {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val javaAnnotation = this as java.lang.annotation.Annotation
    val builder = AnnotationSpec.builder(javaAnnotation.annotationType()).tag<Annotation>(this)
    val methods = annotationType().declaredMethods.sortedBy { it.name }
    for (method in methods) {
      val value = method.invoke(this)
      if (!includeDefaultValues) {
        if (Objects.deepEquals(value, method.defaultValue)) {
          continue
        }
      }
      val member = CodeBlock.builder()
      member.add("%L = ", method.name)
      if (value.javaClass.isArray) {
        member.add("arrayOf(⇥⇥")
        for (i in 0..<Array.getLength(value)) {
          if (i > 0) member.add(", ")
          member.add(memberForValue(Array.get(value, i)))
        }
        member.add("⇤⇤)")
        builder.addMember(member.build())
        continue
      }
      if (value is Annotation) {
        member.add("%L", value.toAnnotationSpec(includeDefaultValues = false))
        builder.addMember(member.build())
        continue
      }
      member.add("%L", memberForValue(value))
      builder.addMember(member.build())
    }
    return builder.build()
  } catch (e: Exception) {
    throw RuntimeException("Reflecting $this failed!", e)
  }
}

/** Creates an [AnnotationSpec] from the given runtime `annotation`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
@JvmOverloads
public fun AnnotationSpec.Companion.get(
  annotation: Annotation,
  includeDefaultValues: Boolean = false,
): AnnotationSpec = annotation.toAnnotationSpec(includeDefaultValues)

/** Creates an [AnnotationSpec] from the given `annotation` mirror. */
@DelicateKaviApi(
  message =
    "Mirror APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
public fun AnnotationSpec.Companion.get(annotation: AnnotationMirror): AnnotationSpec =
  annotation.toAnnotationSpec()

/** Creates an [AnnotationSpec.Builder] for an annotation of the given `type`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
public fun AnnotationSpec.Companion.builder(type: Class<out Annotation>): AnnotationSpec.Builder =
  AnnotationSpec.builder(type.asClassName())

@OptIn(DelicateKaviApi::class)
internal fun AnnotationMirror.toAnnotationSpec(): AnnotationSpec {
  val element = annotationType.asElement() as TypeElement
  val builder = AnnotationSpec.builder(element.asClassName()).tag(this)
  for (executableElement in elementValues.keys) {
    val member = CodeBlock.builder()
    val visitor = Visitor(member)
    val name = executableElement.simpleName.toString()
    member.add("%L = ", name)
    val value = elementValues[executableElement]!!
    value.accept(visitor, name)
    builder.addMember(member.build())
  }
  return builder.build()
}
