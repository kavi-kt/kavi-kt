/*
 * Copyright (C) 2022 Square, Inc.
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

import kotlin.jvm.JvmInline

/** A Kavi spec type that can have a context receiver. */
public interface ContextReceivable {

  /** The originating elements of this type. */
  @ExperimentalKaviApi public val contextReceiverTypes: List<TypeName>

  /** The builder analogue to [ContextReceivable] types. */
  public interface Builder<out T : Builder<T>> {

    /** Mutable map of the current originating elements this builder contains. */
    @ExperimentalKaviApi public val contextReceiverTypes: MutableList<TypeName>

    /** Adds the given [receiverTypes] to this type's list of originating elements. */
    @Suppress("UNCHECKED_CAST")
    @ExperimentalKaviApi
    public fun contextReceivers(receiverTypes: Iterable<TypeName>): T =
      apply { contextReceiverTypes += receiverTypes } as T

    /** Adds the given [receiverTypes] to this type's list of originating elements. */
    @ExperimentalKaviApi
    public fun contextReceivers(vararg receiverTypes: TypeName): T =
      contextReceivers(receiverTypes.toList())
  }
}

@ExperimentalKaviApi
internal fun ContextReceivable.Builder<*>.buildContextReceivers() =
  ContextReceivers(contextReceiverTypes.toImmutableList())

@JvmInline
@ExperimentalKaviApi
internal value class ContextReceivers(override val contextReceiverTypes: List<TypeName>) :
  ContextReceivable
