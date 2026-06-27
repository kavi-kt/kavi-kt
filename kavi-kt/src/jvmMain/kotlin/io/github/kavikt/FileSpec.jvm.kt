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
@file:JvmName("FileSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import javax.annotation.processing.Filer
import javax.tools.JavaFileObject
import javax.tools.JavaFileObject.Kind
import javax.tools.SimpleJavaFileObject
import javax.tools.StandardLocation
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

/**
 * Writes this to [directory] as UTF-8 using the standard directory structure and returns the newly
 * output path.
 */
@Throws(IOException::class)
public fun FileSpec.writeTo(directory: Path): Path {
  require(directory.notExists() || directory.isDirectory()) {
    "path $directory exists but is not a directory."
  }
  val outputPath = directory.resolve(relativePath)
  outputPath.parent.createDirectories()
  outputPath.outputStream().bufferedWriter().use(::writeTo)
  return outputPath
}

/**
 * Writes this to [directory] as UTF-8 using the standard directory structure and returns the newly
 * output file.
 */
@Throws(IOException::class)
public fun FileSpec.writeTo(directory: File): File = writeTo(directory.toPath()).toFile()

/** Writes this to `filer`. */
@Throws(IOException::class)
public fun FileSpec.writeTo(filer: Filer) {
  val originatingElements =
    members
      .asSequence()
      .filterIsInstance<OriginatingElementsHolder>()
      .flatMap { it.originatingElements.asSequence() }
      .toSet()
  val filerSourceFile =
    filer.createResource(
      StandardLocation.SOURCE_OUTPUT,
      packageName,
      "$name.$extension",
      *originatingElements.toTypedArray(),
    )
  try {
    filerSourceFile.openWriter().use { writer -> writeTo(writer) }
  } catch (e: Exception) {
    try {
      filerSourceFile.delete()
    } catch (_: Exception) {}
    throw e
  }
}

/** Adapts this [FileSpec] to a `javax.tools.JavaFileObject`. */
public fun FileSpec.toJavaFileObject(): JavaFileObject {
  val uri = URI.create(relativePath)
  return object : SimpleJavaFileObject(uri, Kind.SOURCE) {
    private val lastModified = System.currentTimeMillis()

    override fun getCharContent(ignoreEncodingErrors: Boolean): String {
      return this@toJavaFileObject.toString()
    }

    override fun openInputStream(): InputStream {
      return ByteArrayInputStream(getCharContent(true).toByteArray(UTF_8))
    }

    override fun getLastModified() = lastModified
  }
}

/** Adds an import for the given `class` and `names`. */
public fun FileSpec.Builder.addImport(
  `class`: Class<*>,
  vararg names: String,
): FileSpec.Builder = apply {
  require(names.isNotEmpty()) { "names array is empty" }
  addImport(`class`.asClassName(), names.toList())
}

/** Adds an import for the given `class` and `names`. */
public fun FileSpec.Builder.addImport(
  `class`: Class<*>,
  names: Iterable<String>,
): FileSpec.Builder = addImport(`class`.asClassName(), names)

/** Adds an aliased import for the given `class`. */
public fun FileSpec.Builder.addAliasedImport(`class`: Class<*>, `as`: String): FileSpec.Builder =
  addAliasedImport(`class`.asClassName(), `as`)

internal actual fun enumConstantClassName(constant: Enum<*>): ClassName =
  constant.declaringJavaClass.asClassName()
