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
@file:JvmName("ClassNames")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@JvmName("get") public fun KClass<*>.asClassName(): ClassName = kClassToClassName(this)

internal expect fun kClassToClassName(kClass: KClass<*>): ClassName

/** Names of the enclosing classes (outermost first) for [this], excluding the package name. */
internal expect fun KClass<*>.enclosingClassNames(qualifiedName: String): List<String>

internal fun KClass<*>.computeClassName(qualifiedName: String): ClassName {
  // First, check for Kotlin types whose enclosing class name is a type that is mapped to a JVM
  // class. Thus, the class backing the nested Kotlin type does not have an enclosing class
  // (i.e., a parent) and the normal algorithm will fail.
  val names =
    when (qualifiedName) {
      "kotlin.Boolean.Companion" -> listOf("kotlin", "Boolean", "Companion")
      "kotlin.Byte.Companion" -> listOf("kotlin", "Byte", "Companion")
      "kotlin.Char.Companion" -> listOf("kotlin", "Char", "Companion")
      "kotlin.Double.Companion" -> listOf("kotlin", "Double", "Companion")
      "kotlin.Enum.Companion" -> listOf("kotlin", "Enum", "Companion")
      "kotlin.Float.Companion" -> listOf("kotlin", "Float", "Companion")
      "kotlin.Int.Companion" -> listOf("kotlin", "Int", "Companion")
      "kotlin.Long.Companion" -> listOf("kotlin", "Long", "Companion")
      "kotlin.Short.Companion" -> listOf("kotlin", "Short", "Companion")
      "kotlin.String.Companion" -> listOf("kotlin", "String", "Companion")
      else -> enclosingClassNames(qualifiedName)
    }

  return ClassName(names)
}
