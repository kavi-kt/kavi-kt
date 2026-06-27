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
@file:JvmName("PropertySpecs")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Type

/** Sets the receiver of this builder to `receiverType`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
public fun PropertySpec.Builder.receiver(receiverType: Type): PropertySpec.Builder =
  receiver(receiverType.asTypeName())

/** Returns a [PropertySpec.Builder] for a property of the given `type`. */
public fun PropertySpec.Companion.builder(
  name: String,
  type: Type,
  vararg modifiers: KModifier,
): PropertySpec.Builder = builder(name, type.asTypeName(), *modifiers)

/** Returns a [PropertySpec.Builder] for a property of the given `type`. */
public fun PropertySpec.Companion.builder(
  name: String,
  type: Type,
  modifiers: Iterable<KModifier>,
): PropertySpec.Builder = builder(name, type.asTypeName(), modifiers)
