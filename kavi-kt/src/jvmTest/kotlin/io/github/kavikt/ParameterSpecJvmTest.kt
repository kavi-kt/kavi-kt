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

import assertk.assertFailure
import assertk.assertions.contains
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.message
import javax.lang.model.element.Modifier
import kotlin.test.Test

class ParameterSpecJvmTest {
  @Suppress("DEPRECATION_ERROR")
  @Test
  fun jvmModifiersAreNotAllowed() {
    assertFailure {
        ParameterSpec.builder("value", INT).jvmModifiers(listOf(Modifier.FINAL)).build()
      }
      .isInstanceOf<IllegalArgumentException>()
      .message()
      .isNotNull()
      .contains("JVM modifiers are not permitted on parameters in Kotlin")
  }
}
