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
@file:JvmName("WildcardTypeNames")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import javax.lang.model.element.TypeParameterElement

internal fun WildcardTypeName.Companion.get(
  mirror: javax.lang.model.type.WildcardType,
  typeVariables: Map<TypeParameterElement, TypeVariableName>,
): TypeName {
  val outType = mirror.extendsBound
  return if (outType == null) {
    val inType = mirror.superBound
    if (inType == null) {
      STAR
    } else {
      WildcardTypeName.consumerOf(TypeName.get(inType, typeVariables))
    }
  } else {
    WildcardTypeName.producerOf(TypeName.get(outType, typeVariables))
  }
}

internal fun WildcardTypeName.Companion.get(
  wildcardName: WildcardType,
  map: MutableMap<Type, TypeVariableName>,
): TypeName {
  return WildcardTypeName(
    wildcardName.upperBounds.map { TypeName.get(it, map = map) },
    wildcardName.lowerBounds.map { TypeName.get(it, map = map) },
  )
}

/** Returns a [WildcardTypeName] that produces `outType`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
public fun WildcardTypeName.Companion.producerOf(outType: Type): WildcardTypeName =
  WildcardTypeName.producerOf(outType.asTypeName())

/** Returns a [WildcardTypeName] that consumes `inType`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
public fun WildcardTypeName.Companion.consumerOf(inType: Type): WildcardTypeName =
  WildcardTypeName.consumerOf(inType.asTypeName())

/** Returns a [TypeName] equivalent to this `javax.lang.model.type.WildcardType`. */
@DelicateKaviApi(
  message =
    "Mirror APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
@JvmName("get")
public fun javax.lang.model.type.WildcardType.asWildcardTypeName(): TypeName =
  WildcardTypeName.get(this, mutableMapOf())

/** Returns a [TypeName] equivalent to this `java.lang.reflect.WildcardType`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
@JvmName("get")
public fun WildcardType.asWildcardTypeName(): TypeName = WildcardTypeName.get(this, mutableMapOf())
