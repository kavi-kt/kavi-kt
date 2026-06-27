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
package io.github.kavikt

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

/**
 * Whole-valued Double/Float literals (e.g. 3.0, 123456.0f) render with a trailing ".0" on
 * JVM/Native but drop it on Kotlin/JS and Wasm, so the expected output is platform-specific.
 */
class CodeBlockTestJvmNative {

  @Test
  fun doublePrecision() {
    val doubles =
      listOf(
        12345678900000.0 to "12_345_678_900_000.0",
        12345678900000.07 to "12_345_678_900_000.07",
        123456.0 to "123_456.0",
        1234.5678 to "1_234.5678",
        12.345678 to "12.345678",
        0.12345678 to "0.12345678",
        0.0001 to "0.0001",
        0.00001 to "0.00001",
        0.000001 to "0.000001",
        0.0000001 to "0.0000001",
      )
    for ((d, expected) in doubles) {
      val a = CodeBlock.of("number %L", d)
      assertThat(a.toString()).isEqualTo("number $expected")
    }
  }

  @Test
  fun floatPrecision() {
    val floats =
      listOf(
        12345678.0f to "12_345_678.0",
        123456.0f to "123_456.0",
        1234.567f to "1_234.567",
        12.34567f to "12.34567",
        0.1234567f to "0.1234567",
        0.0001f to "0.0001",
        0.00001f to "0.00001",
        0.000001f to "0.000001",
        0.0000001f to "0.0000001",
      )
    for ((f, expected) in floats) {
      val a = CodeBlock.of("number %L", f)
      assertThat(a.toString()).isEqualTo("number $expected")
    }
  }

  // https://github.com/square/kotlinpoet/issues/1381
  @Test
  fun useUnderscoresOnLargeDecimalLiterals() {
    assertThat(CodeBlock.of("%L", 10000).toString()).isEqualTo("10_000")
    assertThat(CodeBlock.of("%L", 100000L).toString()).isEqualTo("100_000")
    assertThat(CodeBlock.of("%L", Int.MIN_VALUE).toString()).isEqualTo("-2_147_483_648")
    assertThat(CodeBlock.of("%L", Int.MAX_VALUE).toString()).isEqualTo("2_147_483_647")
    assertThat(CodeBlock.of("%L", Long.MIN_VALUE).toString())
      .isEqualTo("-9_223_372_036_854_775_808")
    assertThat(CodeBlock.of("%L", 10000.123).toString()).isEqualTo("10_000.123")
    assertThat(CodeBlock.of("%L", 3.0).toString()).isEqualTo("3.0")
    assertThat(CodeBlock.of("%L", 10000.123f).toString()).isEqualTo("10_000.123")
    assertThat(CodeBlock.of("%L", 10000.123456789012).toString()).isEqualTo("10_000.123456789011")
    assertThat(CodeBlock.of("%L", 1281.toShort()).toString()).isEqualTo("1_281")

    assertThat(CodeBlock.of("%S", 10000).toString()).isEqualTo("\"10000\"")
    assertThat(CodeBlock.of("%S", 100000L).toString()).isEqualTo("\"100000\"")
    assertThat(CodeBlock.of("%S", Int.MIN_VALUE).toString()).isEqualTo("\"-2147483648\"")
    assertThat(CodeBlock.of("%S", Int.MAX_VALUE).toString()).isEqualTo("\"2147483647\"")
    assertThat(CodeBlock.of("%S", Long.MIN_VALUE).toString()).isEqualTo("\"-9223372036854775808\"")
    assertThat(CodeBlock.of("%S", 10000.123).toString()).isEqualTo("\"10000.123\"")
    assertThat(CodeBlock.of("%S", 3.0).toString()).isEqualTo("\"3.0\"")
    assertThat(CodeBlock.of("%S", 10000.123f).toString()).isEqualTo("\"10000.123\"")
    assertThat(CodeBlock.of("%S", 10000.12345678901).toString()).isEqualTo("\"10000.12345678901\"")
    assertThat(CodeBlock.of("%S", 1281.toShort()).toString()).isEqualTo("\"1281\"")
  }
}
