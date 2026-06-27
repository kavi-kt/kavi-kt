/*
 * Copyright (C) 2018 Square, Inc.
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
@file:JvmName("JvmAnnotations")
@file:JvmMultifileClass

package io.github.kavikt.jvm

import io.github.kavikt.AnnotationSpec
import io.github.kavikt.AnnotationSpec.UseSiteTarget.FILE
import io.github.kavikt.ClassName
import io.github.kavikt.FileSpec
import io.github.kavikt.FunSpec
import io.github.kavikt.FunSpec.Companion.isAccessor
import io.github.kavikt.FunSpec.Companion.isConstructor
import io.github.kavikt.PropertySpec
import io.github.kavikt.TypeName
import io.github.kavikt.TypeSpec
import kotlin.DeprecationLevel.HIDDEN
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Builds the annotation [ClassName] from a string literal rather than a `KClass` token: the
 * kotlin.jvm.* annotations have no JS/Wasm actual, so a `KClass` reference leaves a dangling
 * external signature the Kotlin/JS IR resolver cannot link.
 */
private fun jvmAnnotation(simpleName: String) = ClassName("kotlin.jvm", simpleName)

public fun FileSpec.Builder.jvmName(name: String): FileSpec.Builder =
  addAnnotation(
    AnnotationSpec.builder(jvmAnnotation("JvmName"))
      .useSiteTarget(FILE)
      .addMember("%S", name)
      .build()
  )

public fun FileSpec.Builder.jvmMultifileClass(): FileSpec.Builder =
  addAnnotation(
    AnnotationSpec.builder(jvmAnnotation("JvmMultifileClass")).useSiteTarget(FILE).build()
  )

public fun TypeSpec.Builder.jvmSuppressWildcards(suppress: Boolean = true): TypeSpec.Builder =
  addAnnotation(jvmSuppressWildcardsAnnotation(suppress))

private fun jvmSuppressWildcardsAnnotation(suppress: Boolean = true) =
  AnnotationSpec.builder(jvmAnnotation("JvmSuppressWildcards"))
    .apply { if (!suppress) addMember("suppress = false") }
    .build()

public fun TypeSpec.Builder.jvmInline(): TypeSpec.Builder =
  addAnnotation(jvmAnnotation("JvmInline"))

public fun TypeSpec.Builder.jvmRecord(): TypeSpec.Builder =
  addAnnotation(jvmAnnotation("JvmRecord"))

public fun FunSpec.Builder.jvmStatic(): FunSpec.Builder = apply {
  check(!name.isConstructor) { "Can't apply @JvmStatic to a constructor!" }
  addAnnotation(jvmAnnotation("JvmStatic"))
}

public fun FunSpec.Builder.jvmOverloads(): FunSpec.Builder = apply {
  check(!name.isAccessor) {
    "Can't apply @JvmOverloads to a " + if (name == FunSpec.GETTER) "getter!" else "setter!"
  }
  addAnnotation(jvmAnnotation("JvmOverloads"))
}

public fun FunSpec.Builder.jvmName(name: String): FunSpec.Builder = apply {
  check(!this.name.isConstructor) { "Can't apply @JvmName to a constructor!" }
  addAnnotation(AnnotationSpec.builder(jvmAnnotation("JvmName")).addMember("%S", name).build())
}

public fun FunSpec.Builder.throws(vararg exceptionClasses: TypeName): FunSpec.Builder =
  throws(exceptionClasses.toList())

public fun FunSpec.Builder.throws(exceptionClasses: Iterable<TypeName>): FunSpec.Builder =
  addAnnotation(
    AnnotationSpec.builder(jvmAnnotation("Throws"))
      .apply { exceptionClasses.forEach { addMember("%T::class", it) } }
      .build()
  )

public fun FunSpec.Builder.jvmSuppressWildcards(suppress: Boolean = true): FunSpec.Builder = apply {
  check(!name.isConstructor) { "Can't apply @JvmSuppressWildcards to a constructor!" }
  check(!name.isAccessor) {
    "Can't apply @JvmSuppressWildcards to a " + if (name == FunSpec.GETTER) "getter!" else "setter!"
  }
  addAnnotation(jvmSuppressWildcardsAnnotation(suppress))
}

@Suppress("DEPRECATION", "DEPRECATION_ERROR")
public fun FunSpec.Builder.synchronized(): FunSpec.Builder = apply {
  check(!name.isConstructor) { "Can't apply @Synchronized to a constructor!" }
  addAnnotation(jvmAnnotation("Synchronized"))
}

public fun FunSpec.Builder.strictfp(): FunSpec.Builder = addAnnotation(jvmAnnotation("Strictfp"))

public fun PropertySpec.Builder.jvmField(): PropertySpec.Builder =
  addAnnotation(jvmAnnotation("JvmField"))

public fun PropertySpec.Builder.jvmStatic(): PropertySpec.Builder =
  addAnnotation(jvmAnnotation("JvmStatic"))

public fun PropertySpec.Builder.jvmSuppressWildcards(
  suppress: Boolean = true
): PropertySpec.Builder = addAnnotation(jvmSuppressWildcardsAnnotation(suppress))

public fun PropertySpec.Builder.transient(): PropertySpec.Builder =
  addAnnotation(jvmAnnotation("Transient"))

@Suppress("DEPRECATION", "DEPRECATION_ERROR")
public fun PropertySpec.Builder.volatile(): PropertySpec.Builder =
  addAnnotation(jvmAnnotation("Volatile"))

public fun TypeName.jvmSuppressWildcards(suppress: Boolean = true): TypeName =
  copy(annotations = this.annotations + jvmSuppressWildcardsAnnotation(suppress))

public fun TypeName.jvmWildcard(): TypeName =
  copy(
    annotations = this.annotations + AnnotationSpec.builder(jvmAnnotation("JvmWildcard")).build()
  )

@Suppress("DEPRECATION")
@Deprecated("", level = HIDDEN)
public fun PropertySpec.Builder.jvmDefault(): PropertySpec.Builder = this

@Suppress("DEPRECATION")
@Deprecated("", level = HIDDEN)
public fun FunSpec.Builder.jvmDefault(): FunSpec.Builder = this
