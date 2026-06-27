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
@file:JvmName("FunSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

/** Sets the receiver of this builder to `receiverType`, with optional `kdoc`. */
@JvmOverloads
public fun FunSpec.Builder.receiver(
  receiverType: KClass<*>,
  kdoc: CodeBlock = CodeBlock.EMPTY,
): FunSpec.Builder = receiver(receiverType.asTypeName(), kdoc)

/** Sets the receiver of this builder to `receiverType`, with a formatted `kdoc`. */
public fun FunSpec.Builder.receiver(
  receiverType: KClass<*>,
  kdoc: String,
  vararg args: Any,
): FunSpec.Builder = receiver(receiverType, CodeBlock.of(kdoc, args))

/** Sets the return type of this builder to `returnType`, with optional `kdoc`. */
@JvmOverloads
public fun FunSpec.Builder.returns(
  returnType: KClass<*>,
  kdoc: CodeBlock = CodeBlock.EMPTY,
): FunSpec.Builder = returns(returnType.asTypeName(), kdoc)

/** Sets the return type of this builder to `returnType`, with a formatted `kdoc`. */
public fun FunSpec.Builder.returns(
  returnType: KClass<*>,
  kdoc: String,
  vararg args: Any,
): FunSpec.Builder = returns(returnType.asTypeName(), CodeBlock.of(kdoc, args))

/** Adds a parameter of the given `type` to this builder. */
public fun FunSpec.Builder.addParameter(
  name: String,
  type: KClass<*>,
  vararg modifiers: KModifier,
): FunSpec.Builder = addParameter(name, type.asTypeName(), *modifiers)

/** Adds a parameter of the given `type` to this builder. */
public fun FunSpec.Builder.addParameter(
  name: String,
  type: KClass<*>,
  modifiers: Iterable<KModifier>,
): FunSpec.Builder = addParameter(name, type.asTypeName(), modifiers)
