/*
 * Copyright (C) 2024 Square, Inc.
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

/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
internal actual fun StringBuilder.appendCodePoint(codePoint: CodePoint): StringBuilder {
  // Copied from StringBuilder.kt,
  // TODO Is this correct?
  val code = codePoint.code
  if (code <= Char.MAX_VALUE.code) {
    append(code.toChar())
  } else {
    append(Char.MIN_HIGH_SURROGATE + ((code - 0x10000) ushr 10))
    append(Char.MIN_LOW_SURROGATE + (code and 0x3ff))
  }
  return this
}

/**
 * BMP code points use exact [Char] classification. Kotlin/Native and Kotlin/JS expose no Unicode
 * category data above the BMP, so a supplementary code point (`charCount == 2`) is treated as a
 * non-identifier character and folds to `_`.
 */
internal actual fun CodePoint.isJavaIdentifierStart(): Boolean =
  if (charCount() == 1) Char(code).isJavaIdentifierStart() else false

internal actual fun CodePoint.isJavaIdentifierPart(): Boolean =
  if (charCount() == 1) Char(code).isJavaIdentifierPart() else false

internal actual fun CodePoint.charCount(): Int {
  return if (code >= 0x10000) 2 else 1
}
