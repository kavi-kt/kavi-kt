/*
 * Copyright (C) 2021 Square, Inc.
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
package io.github.kavikt.metadata.specs

import io.github.kavikt.AnnotationSpec

/**
 * Represents a JVM modifier that is represented as an annotation in Kotlin but as a modifier in
 * bytecode. Examples include annotations such as [@JvmStatic][JvmStatic] or
 * [@JvmSynthetic][JvmSynthetic].
 *
 * This API is considered read-only and should not be implemented outside of Kavi.
 */
public interface JvmModifier {
  public fun annotationSpec(): AnnotationSpec? {
    return null
  }
}
