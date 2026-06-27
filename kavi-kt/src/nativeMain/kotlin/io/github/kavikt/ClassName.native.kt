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

import kotlin.reflect.KClass

/**
 * Kotlin/Native exposes `KClass.qualifiedName`, so a [ClassName] is recovered the same way the
 * common code does on the JVM; the enclosing-class chain is parsed from the qualified name.
 */
internal actual fun kClassToClassName(kClass: KClass<*>): ClassName {
  val qualifiedName =
    requireNotNull(kClass.qualifiedName) { "$kClass cannot be represented as a ClassName" }
  return kClass.computeClassName(qualifiedName)
}

/**
 * Recovers the enclosing-class chain from the qualified name using the usual Java convention
 * (lowercase leading segments are the package, the rest are nested class names), mirroring
 * [ClassName.bestGuess].
 */
internal actual fun KClass<*>.enclosingClassNames(qualifiedName: String): List<String> {
  val parts = qualifiedName.split('.')
  var packageEnd = 0
  while (packageEnd < parts.size && parts[packageEnd].firstOrNull()?.isLowerCase() == true) {
    packageEnd++
  }
  val packageName = parts.subList(0, packageEnd).joinToString(".")
  val simpleNames = parts.subList(packageEnd, parts.size)
  return buildList {
    add(packageName)
    addAll(simpleNames)
  }
}
