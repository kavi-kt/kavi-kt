/*
 * Copyright (C) 2024 Square, Inc.
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

/** A spec which can contain [PropertySpec]s and [FunSpec]s. */
public interface MemberSpecHolder {
  public val propertySpecs: List<PropertySpec>
  public val funSpecs: List<FunSpec>

  public interface Builder<out T : Builder<T>> {
    @Suppress("UNCHECKED_CAST")
    public fun addProperties(propertySpecs: Iterable<PropertySpec>): T =
      apply { propertySpecs.map(::addProperty) } as T

    public fun addProperty(propertySpec: PropertySpec): T

    public fun addProperty(name: String, type: TypeName, vararg modifiers: KModifier): T =
      addProperty(PropertySpec.builder(name, type, *modifiers).build())

    public fun addProperty(name: String, type: TypeName, modifiers: Iterable<KModifier>): T =
      addProperty(PropertySpec.builder(name, type, modifiers).build())

    @Suppress("UNCHECKED_CAST")
    public fun addFunctions(funSpecs: Iterable<FunSpec>): T =
      apply { funSpecs.forEach(::addFunction) } as T

    public fun addFunction(funSpec: FunSpec): T
  }
}
