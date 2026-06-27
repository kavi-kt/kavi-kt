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
@file:JvmName("WildcardTypeNames")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Returns a type that represents an unknown type that produces `outType`. For example, if `outType`
 * is `CharSequence`, this returns `out CharSequence`. If `outType` is `Any?`, this returns `*`,
 * which is shorthand for `out Any?`.
 */
public fun WildcardTypeName.Companion.producerOf(outType: KClass<*>): WildcardTypeName =
  WildcardTypeName.producerOf(outType.asTypeName())

/**
 * Returns a type that represents an unknown type that consumes `inType`. For example, if `inType`
 * is `String`, this returns `in String`.
 */
public fun WildcardTypeName.Companion.consumerOf(inType: KClass<*>): WildcardTypeName =
  WildcardTypeName.consumerOf(inType.asTypeName())
