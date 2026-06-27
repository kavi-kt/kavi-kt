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
@file:JvmName("FileSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import okio.FileSystem
import okio.Path

/**
 * Writes this to [directory] within [fileSystem] as UTF-8 using the standard directory structure
 * and returns the newly output path. Pass `okio.FileSystem.SYSTEM` to write to disk.
 */
public fun FileSpec.writeTo(directory: Path, fileSystem: FileSystem): Path {
  require(!fileSystem.exists(directory) || fileSystem.metadata(directory).isDirectory) {
    "path $directory exists but is not a directory."
  }
  val outputPath = directory / relativePath
  outputPath.parent?.let(fileSystem::createDirectories)
  fileSystem.write(outputPath) { writeUtf8(this@writeTo.toString()) }
  return outputPath
}
