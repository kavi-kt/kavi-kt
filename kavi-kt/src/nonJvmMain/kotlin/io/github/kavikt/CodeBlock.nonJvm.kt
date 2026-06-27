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
 * Groups the integer part with `_` and uses `.` as the decimal separator, like the JVM
 * `DecimalFormat` formatter. On Kotlin/Native extreme magnitudes print in scientific notation (e.g.
 * `1.23456789E13`), expanded here to plain positional so `%L` emits the same literal everywhere.
 *
 * Fractional-ness is keyed off the value's string form, not `o is Double`, because on Kotlin/JS
 * every number is a double (`42 is Double`) so the check can't tell an integer from a float. The
 * irreducible gap: a whole-valued Double/Float on Kotlin/JS|Wasm is indistinguishable from an Int
 * and so emitted without `.0`.
 */
internal actual fun formatNumericValue(o: Number): Any? {
  val text = o.toString()
  // Non-finite values have no positional-decimal form (and are not valid Kotlin literals); return
  // the bare `toString()` rather than letting digit-grouping mangle `Infinity` into `In_fin_ity`.
  when (text) {
    "NaN",
    "Infinity",
    "-Infinity" -> return text
  }
  val negative = text.startsWith("-")
  val unsigned0 = if (negative) text.substring(1) else text
  val scientific = unsigned0.indexOf('E') != -1 || unsigned0.indexOf('e') != -1
  val unsigned = if (scientific) expandScientificNotation(unsigned0) else unsigned0

  val dotIndex = unsigned.indexOf('.')
  // A floating-point value carries a decimal point (after any scientific expansion); an integer
  // value never does. This is the only cross-platform-reliable signal (see the KDoc above).
  val isFractional = dotIndex != -1
  val integerPart = if (dotIndex == -1) unsigned else unsigned.substring(0, dotIndex)
  val fractionPart = if (dotIndex == -1) "" else unsigned.substring(dotIndex + 1)

  // Match the JVM DecimalFormat pattern, which keeps at least one fraction digit and drops trailing
  // zeros beyond the significant ones (e.g. `1.0E-4` -> `0.0001`, not `0.00010`).
  val trimmedFraction = fractionPart.trimEnd('0')

  val grouped = integerPart.reversed().chunked(3).joinToString("_").reversed()

  return buildString {
    if (negative) append('-')
    append(grouped)
    if (isFractional) {
      append('.')
      append(if (trimmedFraction.isEmpty()) "0" else trimmedFraction)
    }
  }
}

/**
 * Expands an unsigned scientific-notation decimal string (mantissa `d.ddd` with a signed
 * power-of-ten exponent, the Kotlin/Native `toString()` shape) into a plain positional decimal
 * string by shifting the decimal point. Stdlib-only, with no `BigDecimal`.
 */
private fun expandScientificNotation(value: String): String {
  val eIndex = value.indexOfFirst { it == 'e' || it == 'E' }
  val mantissa = value.substring(0, eIndex)
  val exponent = value.substring(eIndex + 1).toInt()

  val mantissaDot = mantissa.indexOf('.')
  val mantissaDigits =
    if (mantissaDot == -1) mantissa
    else mantissa.substring(0, mantissaDot) + mantissa.substring(mantissaDot + 1)
  // Position of the decimal point within `mantissaDigits` before applying the exponent.
  val basePointPosition = if (mantissaDot == -1) mantissa.length else mantissaDot
  val pointPosition = basePointPosition + exponent

  return when {
    pointPosition <= 0 -> "0." + "0".repeat(-pointPosition) + mantissaDigits
    pointPosition >= mantissaDigits.length ->
      mantissaDigits + "0".repeat(pointPosition - mantissaDigits.length) + ".0"
    else ->
      mantissaDigits.substring(0, pointPosition) + "." + mantissaDigits.substring(pointPosition)
  }
}
