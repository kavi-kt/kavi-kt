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
package io.github.kavikt

import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/** Returns a [TypeName] equivalent to this [KClass]. */
@JvmName("get") public fun KClass<*>.asTypeName(): ClassName = asClassName()

/**
 * Returns a [TypeName] equivalent of the reified type parameter [T] using reflection, maybe using
 * kotlin-reflect if required.
 */
public inline fun <reified T> typeNameOf(): TypeName = typeOf<T>().asTypeName()

/**
 * Returns a copy of this [TypeName] with annotations created from the specified [annotations].
 *
 * This is a convenience method that simplifies adding annotations to a type:
 * ```kotlin
 * val annotatedType = String::class.asTypeName()
 *   .annotated(MyAnnotation::class)
 * ```
 */
public fun TypeName.annotated(vararg annotations: KClass<out Annotation>): TypeName =
  copy(annotations = this.annotations + annotations.map { AnnotationSpec.builder(it).build() })
