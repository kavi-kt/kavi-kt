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
package io.github.kavikt

import kotlin.jvm.JvmInline

/** Represents a context parameter with a name and type. */
public class ContextParameter(public val name: String, public val type: TypeName) {
  public constructor(type: TypeName) : this(name = "_", type)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ContextParameter) return false

    if (name != other.name) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

  override fun toString(): String = "$name: $type"
}

/** A Kavi spec type that can have context parameters. */
public interface ContextParameterizable {
  /** The context parameters of this type. */
  @ExperimentalKaviApi public val contextParameters: List<ContextParameter>

  /** The builder analogue to [ContextParameterizable] types. */
  public interface Builder<out T : Builder<T>> {
    /** Mutable list of the current context parameters this builder contains. */
    @ExperimentalKaviApi public val contextParameters: MutableList<ContextParameter>

    /** Adds the given [parameters] to this type's list of context parameters. */
    @Suppress("UNCHECKED_CAST")
    @ExperimentalKaviApi
    public fun contextParameters(parameters: Iterable<ContextParameter>): T =
      apply { contextParameters += parameters } as T

    /**
     * Adds a context parameter with the given [name] and [type] to this type's list of context
     * parameters.
     */
    @ExperimentalKaviApi
    public fun contextParameter(name: String, type: TypeName): T =
      contextParameters(listOf(ContextParameter(name, type)))

    /**
     * Adds a context parameter with the name "_" and [type] to this type's list of context
     * parameters.
     */
    @ExperimentalKaviApi
    public fun contextParameter(type: TypeName): T =
      contextParameters(listOf(ContextParameter(type)))

    /** Adds the given [ContextParameter] to this type's list of context parameters. */
    @ExperimentalKaviApi
    public fun contextParameter(contextParameter: ContextParameter): T =
      contextParameters(listOf(contextParameter))
  }
}

@ExperimentalKaviApi
internal fun ContextParameterizable.Builder<*>.buildContextParameters() =
  ContextParameters(contextParameters.toImmutableList())

@JvmInline
@ExperimentalKaviApi
internal value class ContextParameters(override val contextParameters: List<ContextParameter>) :
  ContextParameterizable
