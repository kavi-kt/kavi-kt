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
@file:JvmName("Taggables")

package io.github.kavikt

/**
 * Attaches [tag] to the request using [T] as a key. Tags can be read from a request using
 * [Taggable.tag]. Use `null` to remove any existing tag assigned for [T].
 *
 * Use this API to attach debugging or other application data to a spec so that you may read it in
 * other APIs or callbacks.
 */
public inline fun <reified T : Any> FileSpec.Builder.tag(tag: T?): FileSpec.Builder =
  tag(T::class, tag)

/** Returns the tag attached with [type] as a key, or null if no tag is attached with that key. */
public fun <T : Any> Taggable.tag(type: Class<T>): T? = tag(type.kotlin)

/**
 * Attaches [tag] to the request using [type] as a key. Tags can be read from a request using
 * [Taggable.tag]. Use `null` to remove any existing tag assigned for [type].
 *
 * Use this API to attach originating elements, debugging, or other application data to a spec so
 * that you may read it in other APIs or callbacks.
 */
public fun <T : Taggable.Builder<T>> Taggable.Builder<T>.tag(type: Class<*>, tag: Any?): T =
  tag(type.kotlin, tag)
