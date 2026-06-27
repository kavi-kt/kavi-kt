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
@file:JvmName("TypeVariableNames")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVariance

/** Returns a [TypeVariableName] equivalent to this [KTypeParameter]. */
public fun KTypeParameter.asTypeVariableName(): TypeVariableName {
  return asTypeVariableName(mutableMapOf())
}

/**
 * Internal method for resolving type parameters with cycle detection. This is used to avoid
 * infinite recursion when dealing with recursively bound generics.
 */
internal fun KTypeParameter.asTypeVariableName(
  map: MutableMap<KTypeParameter, TypeVariableName>
): TypeVariableName {
  var result: TypeVariableName? = map[this]
  if (result == null) {
    val bounds = mutableListOf<TypeName>()
    val visibleBounds = bounds.toReadOnlyList()
    result =
      TypeVariableName(
        name = name,
        bounds = visibleBounds,
        variance =
          when (variance) {
            KVariance.INVARIANT -> null
            KVariance.IN -> KModifier.IN
            KVariance.OUT -> KModifier.OUT
          },
      )
    map[this] = result

    for (bound in upperBounds) {
      bounds += bound.asTypeName(map)
    }

    bounds.remove(ANY)
    if (bounds.isEmpty()) {
      bounds.add(NULLABLE_ANY)
    }
  }
  return result
}

/** Returns type variable named `name` with `variance` and `bounds`. */
@JvmName("get")
@JvmOverloads
public operator fun TypeVariableName.Companion.invoke(
  name: String,
  vararg bounds: KClass<*>,
  variance: KModifier? = null,
): TypeVariableName = TypeVariableName(name, bounds.map { it.asTypeName() }, variance)

/** Returns type variable named `name` with `variance` and `bounds`. */
@JvmName("getWithClasses")
@JvmOverloads
public operator fun TypeVariableName.Companion.invoke(
  name: String,
  bounds: Iterable<KClass<*>>,
  variance: KModifier? = null,
): TypeVariableName = TypeVariableName(name, bounds.map { it.asTypeName() }, variance)
