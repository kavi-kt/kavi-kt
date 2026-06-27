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
@file:JvmName("ParameterizedTypeNames")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

public class ParameterizedTypeName
internal constructor(
  private val enclosingType: TypeName?,
  public val rawType: ClassName,
  typeArguments: List<TypeName>,
  nullable: Boolean = false,
  annotations: List<AnnotationSpec> = emptyList(),
  tags: Map<KClass<*>, Any> = emptyMap(),
) : TypeName(nullable, annotations, TagMap(tags)) {
  public val typeArguments: List<TypeName> = typeArguments.toImmutableList()

  init {
    require(typeArguments.isNotEmpty() || enclosingType != null) { "no type arguments: $rawType" }
  }

  override fun copy(
    nullable: Boolean,
    annotations: List<AnnotationSpec>,
    tags: Map<KClass<*>, Any>,
  ): ParameterizedTypeName {
    return ParameterizedTypeName(enclosingType, rawType, typeArguments, nullable, annotations, tags)
  }

  public fun copy(
    nullable: Boolean = this.isNullable,
    annotations: List<AnnotationSpec> = this.annotations,
    tags: Map<KClass<*>, Any> = this.tags,
    typeArguments: List<TypeName> = this.typeArguments,
  ): ParameterizedTypeName {
    return ParameterizedTypeName(enclosingType, rawType, typeArguments, nullable, annotations, tags)
  }

  public fun plusParameter(typeArgument: TypeName): ParameterizedTypeName =
    ParameterizedTypeName(
      enclosingType,
      rawType,
      typeArguments + typeArgument,
      isNullable,
      annotations,
    )

  override fun emit(out: CodeWriter): CodeWriter {
    if (enclosingType != null) {
      enclosingType.emitAnnotations(out)
      enclosingType.emit(out)
      out.emit("." + rawType.simpleName)
    } else {
      rawType.emitAnnotations(out)
      rawType.emit(out)
    }
    if (typeArguments.isNotEmpty()) {
      out.emit("<")
      typeArguments.forEachIndexed { index, parameter ->
        if (index > 0) out.emit(", ")
        parameter.emitAnnotations(out)
        parameter.emit(out)
        parameter.emitNullable(out)
      }
      out.emit(">")
    }
    return out
  }

  /**
   * Returns a new [ParameterizedTypeName] instance for the specified `name` as nested inside this
   * class, with the specified `typeArguments`.
   */
  public fun nestedClass(name: String, typeArguments: List<TypeName>): ParameterizedTypeName =
    ParameterizedTypeName(this, rawType.nestedClass(name), typeArguments)

  override fun equalsWithGuard(other: TypeName, seen: RecursiveComparison): Boolean {
    if (!super.equalsWithGuard(other, seen)) return false

    other as ParameterizedTypeName

    if (!enclosingType.deepEquals(other.enclosingType, seen)) return false
    if (!rawType.deepEquals(other.rawType, seen)) return false
    if (!typeArguments.deepEquals(other.typeArguments, seen)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (enclosingType?.hashCode() ?: 0)
    result = 31 * result + rawType.hashCode()
    result = 31 * result + typeArguments.hashCode()
    return result
  }

  public companion object {
    /** Returns a parameterized type, applying `typeArguments` to `this`. */
    @JvmStatic
    @JvmName("get")
    public fun ClassName.parameterizedBy(vararg typeArguments: TypeName): ParameterizedTypeName =
      ParameterizedTypeName(null, this, typeArguments.toList())

    /** Returns a parameterized type, applying `typeArguments` to `this`. */
    @JvmStatic
    @JvmName("get")
    public fun ClassName.parameterizedBy(typeArguments: List<TypeName>): ParameterizedTypeName =
      ParameterizedTypeName(null, this, typeArguments)

    /** Returns a parameterized type, applying `typeArgument` to `this`. */
    @JvmStatic
    @JvmName("get")
    public fun ClassName.plusParameter(typeArgument: TypeName): ParameterizedTypeName =
      parameterizedBy(typeArgument)
  }
}
