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
package io.github.kavikt

import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

/**
 * Reconstructs a [ParameterizedTypeName] from the stdlib `KType` API. A nested generic class is
 * rejected because per-level type-parameter arity is unavailable without kotlin-reflect.
 */
internal actual fun ParameterizedTypeName.Companion.get(
  type: KClass<*>,
  nullable: Boolean,
  typeArguments: List<KTypeProjection>,
  map: MutableMap<KTypeParameter, TypeVariableName>,
): TypeName {
  if (typeArguments.isEmpty()) {
    return type.asTypeName().run { if (nullable) copy(nullable = true) else this }
  }

  val rawType = type.asTypeName()

  if (rawType.simpleNames.size > 1) {
    val segments =
      (listOf(rawType.packageName) + rawType.simpleNames).joinToString(", ") { "\"$it\"" }
    throw UnsupportedOperationException(
      "Cannot reflect the nested generic type ${rawType.canonicalName} on Kotlin/Native: " +
        "runtime reflection exposes no type-parameter arity, so its type arguments cannot be " +
        "distributed across nesting levels. Build it structurally instead, e.g. " +
        "ClassName($segments).parameterizedBy(...)."
    )
  }

  return ParameterizedTypeName(
    null,
    rawType,
    typeArguments.map { (paramVariance, paramType) ->
      val typeName = paramType?.asTypeName(map) ?: return@map STAR
      when (paramVariance) {
        null -> STAR
        KVariance.INVARIANT -> typeName
        KVariance.IN -> WildcardTypeName.consumerOf(typeName)
        KVariance.OUT -> WildcardTypeName.producerOf(typeName)
      }
    },
    nullable,
    emptyList(),
  )
}
