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
@file:JvmName("PropertySpecs")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/** Sets the receiver of this builder to `receiverType`. */
public fun PropertySpec.Builder.receiver(receiverType: KClass<*>): PropertySpec.Builder =
  receiver(receiverType.asTypeName())

/**
 * Specify the type of this property's backing field. If different from [type], an explicit backing
 * field will be emitted (see
 * [KEEP-0430](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0430-explicit-backing-fields.md)):
 * ```kotlin
 * val city: LiveData<String> field: MutableLiveData<String>
 * ```
 *
 * Setting backing field type to the same value as [type] has no effect.
 */
public fun PropertySpec.Builder.backingFieldType(type: KClass<*>): PropertySpec.Builder =
  backingFieldType(type.asTypeName())

/** Returns a [PropertySpec.Builder] for a property of the given `type`. */
public fun PropertySpec.Companion.builder(
  name: String,
  type: KClass<*>,
  vararg modifiers: KModifier,
): PropertySpec.Builder = builder(name, type.asTypeName(), *modifiers)

/** Returns a [PropertySpec.Builder] for a property of the given `type`. */
public fun PropertySpec.Companion.builder(
  name: String,
  type: KClass<*>,
  modifiers: Iterable<KModifier>,
): PropertySpec.Builder = builder(name, type.asTypeName(), modifiers)
