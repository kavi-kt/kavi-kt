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

// A hand-rolled scanner over Char.category mirroring IDENTIFIER_REGEX_VALUE (commonMain): that
// regex's \p{gc=...} Unicode-category classes can't be parsed by the Kotlin/Native regex engine, so
// building the Regex throws at file-init (KT-71003).

private val LETTER_CATEGORIES =
  setOf(
    CharCategory.UPPERCASE_LETTER, // Lu
    CharCategory.LOWERCASE_LETTER, // Ll
    CharCategory.TITLECASE_LETTER, // Lt
    CharCategory.MODIFIER_LETTER, // Lm
    CharCategory.OTHER_LETTER, // Lo
    CharCategory.LETTER_NUMBER, // Nl
  )

// Rank of each letter category within the strict trailing order Lu < Ll < Lt < Lm < Lo < Nl. Used
// only for the post-digit tail; -1 marks a non-letter (which terminates the tail / fails the
// match).
private fun Char.letterOrderRank(): Int =
  when (category) {
    CharCategory.UPPERCASE_LETTER -> 0
    CharCategory.LOWERCASE_LETTER -> 1
    CharCategory.TITLECASE_LETTER -> 2
    CharCategory.MODIFIER_LETTER -> 3
    CharCategory.OTHER_LETTER -> 4
    CharCategory.LETTER_NUMBER -> 5
    else -> -1
  }

private fun String.matchesUnquotedIdentifier(): Boolean {
  if (isEmpty()) return false

  var i = 0
  // One-or-more leading letters, any letter category, unordered.
  if (this[i].category !in LETTER_CATEGORIES) return false
  while (i < length && this[i].category in LETTER_CATEGORIES) i++

  // A single run of ASCII digits; Unicode digits (Nd) are deliberately excluded.
  while (i < length && this[i] in '0'..'9') i++

  // Trailing letters, each at least the previous one's rank in the fixed order.
  var minRank = 0
  while (i < length) {
    val rank = this[i].letterOrderRank()
    if (rank < minRank) return false
    minRank = rank
    i++
  }
  return true
}

private fun String.matchesBacktickQuotedIdentifier(): Boolean {
  // Backtick, one-or-more chars (none of newline, carriage return, backtick), closing backtick. The
  // length-3 floor rejects the empty `` and the lone ```.
  if (length < 3) return false
  if (this[0] != '`' || this[length - 1] != '`') return false
  for (index in 1 until length - 1) {
    val c = this[index]
    if (c == '\n' || c == '\r' || c == '`') return false
  }
  return true
}

internal actual val String.isIdentifier: Boolean
  get() = matchesUnquotedIdentifier() || matchesBacktickQuotedIdentifier()
