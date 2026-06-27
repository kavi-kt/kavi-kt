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
@file:JvmName("ParameterizedTypeNames")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

/** Returns a parameterized type equivalent to `type`. */
internal fun ParameterizedTypeName.Companion.get(
  type: ParameterizedType,
  map: MutableMap<Type, TypeVariableName>,
): ParameterizedTypeName {
  val rawType = (type.rawType as Class<*>).asClassName()
  val ownerType =
    if (
      type.ownerType is ParameterizedType &&
        !Modifier.isStatic((type.rawType as Class<*>).modifiers)
    ) {
      type.ownerType as ParameterizedType
    } else {
      null
    }

  val typeArguments = type.actualTypeArguments.map { TypeName.get(it, map = map) }
  return if (ownerType != null) {
    get(ownerType, map = map).nestedClass(rawType.simpleName, typeArguments)
  } else {
    ParameterizedTypeName(null, rawType, typeArguments)
  }
}

/** Returns a type name equivalent to type with given list of type arguments. */
internal actual fun ParameterizedTypeName.Companion.get(
  type: KClass<*>,
  nullable: Boolean,
  typeArguments: List<KTypeProjection>,
  map: MutableMap<KTypeParameter, TypeVariableName>,
): TypeName {
  if (typeArguments.isEmpty()) {
    return type.asTypeName().run { if (nullable) copy(nullable = true) else this }
  }

  val effectiveType = if (type.java.isArray) Array<Unit>::class else type
  val enclosingClass = type.java.enclosingClass?.kotlin

  return ParameterizedTypeName(
    enclosingClass?.let {
      get(it, false, typeArguments.drop(effectiveType.typeParameters.size), map)
    },
    effectiveType.asTypeName(),
    typeArguments.take(effectiveType.typeParameters.size).map { (paramVariance, paramType) ->
      val typeName = paramType?.asTypeName(map) ?: return@map STAR
      when (paramVariance) {
        null -> STAR
        KVariance.INVARIANT -> typeName
        KVariance.IN -> WildcardTypeName.consumerOf(typeName)
        KVariance.OUT -> WildcardTypeName.producerOf(typeName)
      }
    },
    nullable,
    effectiveType.annotations.map { AnnotationSpec.get(it) },
  )
}

/** Returns a parameterized type, applying `typeArguments` to `this`. */
@JvmName("get")
public fun Class<*>.parameterizedBy(vararg typeArguments: Type): ParameterizedTypeName =
  ParameterizedTypeName(null, asClassName(), typeArguments.map { it.asTypeName() })

/** Returns a parameterized type, applying `typeArguments` to `this`. */
@JvmName("get")
public fun Class<*>.parameterizedBy(typeArguments: Iterable<Type>): ParameterizedTypeName =
  ParameterizedTypeName(null, asClassName(), typeArguments.map { it.asTypeName() })

/** Returns a parameterized type, applying `typeArgument` to `this`. */
@JvmName("get")
public fun Class<*>.plusParameter(typeArgument: Class<*>): ParameterizedTypeName =
  parameterizedBy(typeArgument)

/** Returns a new [ParameterizedTypeName] with `typeArgument` appended. */
public fun ParameterizedTypeName.plusParameter(typeArgument: Class<*>): ParameterizedTypeName =
  plusParameter(typeArgument.asClassName())

/** Returns a parameterized type equivalent to `type`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
@JvmName("get")
public fun ParameterizedType.asParameterizedTypeName(): ParameterizedTypeName =
  ParameterizedTypeName.get(this, mutableMapOf())
