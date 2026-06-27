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

internal actual fun String.codePointAt(index: Int): CodePoint {
  val high = this[index]
  if (high.isHighSurrogate() && index + 1 < length) {
    val low = this[index + 1]
    if (low.isLowSurrogate()) {
      return CodePoint(0x10000 + ((high.code - 0xD800) shl 10) + (low.code - 0xDC00))
    }
  }
  return CodePoint(high.code)
}

/**
 * BMP code points use exact [Char] classification; supplementary code points (`charCount == 2`)
 * have no single-[Char] form and return `false`.
 */
internal actual fun CodePoint.isLowerCase(): Boolean =
  if (charCount() == 1) Char(code).isLowerCase() else false

internal actual fun CodePoint.isUpperCase(): Boolean =
  if (charCount() == 1) Char(code).isUpperCase() else false
