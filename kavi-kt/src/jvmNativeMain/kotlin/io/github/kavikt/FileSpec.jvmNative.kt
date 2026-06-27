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
@file:JvmName("FileSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/** Adds an import for the given `class` and `names`. */
public fun FileSpec.Builder.addImport(
  `class`: KClass<*>,
  vararg names: String,
): FileSpec.Builder = apply {
  require(names.isNotEmpty()) { "names array is empty" }
  addImport(`class`.asClassName(), names.toList())
}

/** Adds an import for the given `class` and `names`. */
public fun FileSpec.Builder.addImport(
  `class`: KClass<*>,
  names: Iterable<String>,
): FileSpec.Builder = addImport(`class`.asClassName(), names)

/** Adds an aliased import for the given `class`. */
public fun FileSpec.Builder.addAliasedImport(`class`: KClass<*>, `as`: String): FileSpec.Builder =
  addAliasedImport(`class`.asClassName(), `as`)

/** Resolves the [ClassName] of the type declaring the given enum [constant]. */
internal expect fun enumConstantClassName(constant: Enum<*>): ClassName

/** Adds an import for [constant]'s declaring enum class, aliased to the constant's name. */
public fun FileSpec.Builder.addImport(constant: Enum<*>): FileSpec.Builder =
  addImport(enumConstantClassName(constant), constant.name)
