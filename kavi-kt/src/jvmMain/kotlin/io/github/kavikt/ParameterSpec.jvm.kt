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
@file:JvmName("ParameterSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Type
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

/** Returns a [ParameterSpec] from the given [element]. */
@OptIn(DelicateKaviApi::class)
@DelicateKaviApi(
  message =
    "Element APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
public fun ParameterSpec.Companion.get(element: VariableElement): ParameterSpec {
  val name = element.simpleName.toString()
  val type = element.asType().asTypeName()
  return ParameterSpec.builder(name, type).build()
}

/** Returns the [ParameterSpec]s of `method`. */
@OptIn(DelicateKaviApi::class)
@DelicateKaviApi(
  message =
    "Element APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
public fun ParameterSpec.Companion.parametersOf(method: ExecutableElement): List<ParameterSpec> =
  method.parameters.map { ParameterSpec.get(it) }

/** Returns a [ParameterSpec.Builder] for a parameter of the given `type`. */
public fun ParameterSpec.Companion.builder(
  name: String,
  type: Type,
  vararg modifiers: KModifier,
): ParameterSpec.Builder = builder(name, type.asTypeName(), *modifiers)

/** Returns a [ParameterSpec.Builder] for a parameter of the given `type`. */
public fun ParameterSpec.Companion.builder(
  name: String,
  type: Type,
  modifiers: Iterable<KModifier>,
): ParameterSpec.Builder = builder(name, type.asTypeName(), modifiers)

/** Returns an unnamed [ParameterSpec] of the given `type`. */
public fun ParameterSpec.Companion.unnamed(type: Type): ParameterSpec = unnamed(type.asTypeName())

/** No JVM modifiers apply to Kotlin parameters; calling this always fails. */
@Deprecated(
  "There are no jvm modifiers applicable to parameters in Kotlin",
  ReplaceWith(""),
  level = DeprecationLevel.ERROR,
)
public fun ParameterSpec.Builder.jvmModifiers(
  @Suppress("UNUSED_PARAMETER", "unused") modifiers: Iterable<Modifier>
): ParameterSpec.Builder = apply {
  throw IllegalArgumentException("JVM modifiers are not permitted on parameters in Kotlin")
}
