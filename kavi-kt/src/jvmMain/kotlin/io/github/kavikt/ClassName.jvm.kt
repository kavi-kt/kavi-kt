/*
 * Copyright (C) 2014 Google, Inc.
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
@file:JvmName("ClassNames")
@file:JvmMultifileClass

package io.github.kavikt

import java.util.ArrayDeque
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.NestingKind.MEMBER
import javax.lang.model.element.NestingKind.TOP_LEVEL
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

@DelicateKaviApi(
  message =
    "Java reflection APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead.",
)
@JvmName("get")
public fun Class<*>.asClassName(): ClassName {
  require(!isPrimitive) { "primitive types cannot be represented as a ClassName" }
  require(Void.TYPE != this) { "'void' type cannot be represented as a ClassName" }
  require(!isArray) { "array types cannot be represented as a ClassName" }
  val names = mutableListOf<String>()
  var c = this
  while (true) {
    names += c.simpleName
    val enclosing = c.enclosingClass ?: break
    c = enclosing
  }
  // Avoid unreliable Class.getPackage(). https://github.com/square/javapoet/issues/295
  val lastDot = c.name.lastIndexOf('.')
  if (lastDot != -1) names += c.name.substring(0, lastDot)
  names.reverse()
  return ClassName(names)
}

internal actual fun kClassToClassName(kClass: KClass<*>): ClassName {
  val qualifiedName =
    requireNotNull(kClass.qualifiedName) { "$kClass cannot be represented as a ClassName" }
  return kClass.computeClassName(qualifiedName)
}

internal actual fun KClass<*>.enclosingClassNames(qualifiedName: String): List<String> {
  var remaining = qualifiedName
  val names = ArrayDeque<String>()
  var target: Class<*>? = java
  while (target != null) {
    target = target.enclosingClass

    val dot = remaining.lastIndexOf('.')
    if (dot == -1) {
      if (target != null) throw AssertionError(this) // More enclosing classes than dots.
      names.addFirst(remaining)
      remaining = ""
    } else {
      names.addFirst(remaining.substring(dot + 1))
      remaining = remaining.substring(0, dot)
    }
  }

  names.addFirst(remaining)
  return names.toList()
}

/** Returns the class name for `element`. */
@DelicateKaviApi(
  message =
    "Element APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead.",
)
@JvmName("get")
public fun TypeElement.asClassName(): ClassName {
  fun isClassOrInterface(e: Element) = e.kind.isClass || e.kind.isInterface

  fun getPackage(type: Element): PackageElement {
    var t = type
    while (t.kind != ElementKind.PACKAGE) {
      t = t.enclosingElement
    }
    return t as PackageElement
  }

  val names = mutableListOf<String>()
  var e: Element = this
  while (isClassOrInterface(e)) {
    val eType = e as TypeElement
    require(eType.nestingKind.isOneOf(TOP_LEVEL, MEMBER)) { "unexpected type testing" }
    names += eType.simpleName.toString()
    e = eType.enclosingElement
  }
  names += getPackage(this).qualifiedName.toString()
  names.reverse()
  return ClassName(names)
}
