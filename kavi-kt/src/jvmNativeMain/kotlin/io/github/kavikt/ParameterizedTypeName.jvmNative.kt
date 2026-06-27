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
@file:JvmName("ParameterizedTypeNames")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

/** Returns a parameterized type, applying `typeArguments` to `this`. */
@JvmName("get")
public fun KClass<*>.parameterizedBy(vararg typeArguments: KClass<*>): ParameterizedTypeName =
  ParameterizedTypeName(null, asClassName(), typeArguments.map { it.asTypeName() })

/** Returns a parameterized type, applying `typeArguments` to `this`. */
@JvmName("get")
public fun KClass<*>.parameterizedBy(typeArguments: Iterable<KClass<*>>): ParameterizedTypeName =
  ParameterizedTypeName(null, asClassName(), typeArguments.map { it.asTypeName() })

/** Returns a parameterized type, applying `typeArgument` to `this`. */
@JvmName("get")
public fun KClass<*>.plusParameter(typeArgument: KClass<*>): ParameterizedTypeName =
  parameterizedBy(typeArgument)

/** Returns a type name equivalent to type with given list of type arguments. */
internal expect fun ParameterizedTypeName.Companion.get(
  type: KClass<*>,
  nullable: Boolean,
  typeArguments: List<KTypeProjection>,
  map: MutableMap<KTypeParameter, TypeVariableName> = mutableMapOf(),
): TypeName

/**
 * Returns a [TypeName] equivalent to the given Kotlin KType using reflection, maybe using
 * kotlin-reflect if required.
 */
public fun KType.asTypeName(): TypeName = asTypeName(mutableMapOf())

/**
 * The safe, opt-out counterpart to [asTypeName]: returns `null` when the platform's reflection
 * cannot reconstruct the runtime type, signalling the caller to build it structurally instead.
 *
 * This happens on Kotlin/Native for a nested generic class (e.g. `Map.Entry<K, V>`), and on
 * Kotlin/JS and Kotlin/Wasm for the whole KType reflection path. A malformed [KType] still throws
 * [IllegalArgumentException] — that is a programming error, not an unsupported platform.
 */
public fun KType.asTypeNameOrNull(): TypeName? =
  try {
    asTypeName()
  } catch (_: UnsupportedOperationException) {
    // Runtime type not reflectable here; IllegalArgumentException (malformed input) is not caught.
    null
  }

/**
 * Internal method for resolving KType with cycle detection. This is used to avoid infinite
 * recursion when dealing with recursively bound generics.
 */
internal fun KType.asTypeName(map: MutableMap<KTypeParameter, TypeVariableName>): TypeName {
  val classifier = this.classifier
  if (classifier is KTypeParameter) {
    return classifier.asTypeVariableName(map).run {
      if (isMarkedNullable) copy(nullable = true) else this
    }
  }

  if (classifier == null || classifier !is KClass<*>) {
    throw IllegalArgumentException("Cannot build TypeName for $this")
  }

  return ParameterizedTypeName.get(classifier, this.isMarkedNullable, this.arguments, map)
}

/** Returns a parameterized type, applying `typeArgument` to `this`. */
public fun ParameterizedTypeName.plusParameter(typeArgument: KClass<*>): ParameterizedTypeName =
  plusParameter(typeArgument.asClassName())
