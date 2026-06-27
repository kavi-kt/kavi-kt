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

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class FileSpecOkioTest {
  @Test
  fun writeToOkioFileSystem() {
    val fileSystem = FakeFileSystem()
    val source =
      FileSpec.builder("com.squareup.tacos", "Taco")
        .addType(TypeSpec.classBuilder("Taco").build())
        .build()

    val outputDirectory = "/output".toPath()
    fileSystem.createDirectories(outputDirectory)
    val outputPath = source.writeTo(outputDirectory, fileSystem)

    assertThat(outputPath).isEqualTo("/output/com/squareup/tacos/Taco.kt".toPath())
    assertThat(fileSystem.read(outputPath) { readUtf8() })
      .isEqualTo(
        """
        |package com.squareup.tacos
        |
        |public class Taco
        |"""
          .trimMargin()
      )
  }

  @Test
  fun writeToRejectsNonDirectory() {
    val fileSystem = FakeFileSystem()
    val notADirectory = "/file.txt".toPath()
    fileSystem.write(notADirectory) { writeUtf8("nope") }
    val source = FileSpec.builder("com.squareup.tacos", "Taco").build()

    val failure = runCatching { source.writeTo(notADirectory, fileSystem) }.exceptionOrNull()
    assertThat(failure is IllegalArgumentException).isEqualTo(true)
  }
}
