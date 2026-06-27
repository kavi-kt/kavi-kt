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

/**
 * Common stand-in for `kotlin.jvm.JvmDefaultWithCompatibility` (which is JVM-only). On the JVM it
 * `actual typealias`es to the real annotation, preserving the interface default-method ABI (the
 * `$DefaultImpls` bridge) under the project's global `-jvm-default=no-compatibility`. Off the JVM
 * `@OptionalExpectation` lets it vanish — no actual needed.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
public expect annotation class JvmDefaultWithCompatibility()
