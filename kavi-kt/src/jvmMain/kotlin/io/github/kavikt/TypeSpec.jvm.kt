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
@file:JvmName("TypeSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import java.lang.reflect.Type

/** Sets the superclass of this builder to `superclass`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
public fun TypeSpec.Builder.superclass(superclass: Type): TypeSpec.Builder =
  superclass(superclass.asTypeName())

/** Adds `superinterface` to this builder, optionally delegating to it via `delegate`. */
@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider " +
      "using the kavi-kt-metadata APIs instead."
)
@JvmOverloads
public fun TypeSpec.Builder.addSuperinterface(
  superinterface: Type,
  delegate: CodeBlock = CodeBlock.EMPTY,
): TypeSpec.Builder = addSuperinterface(superinterface.asTypeName(), delegate)
