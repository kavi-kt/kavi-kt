/*
 * Copyright (C) 2019 Square, Inc.
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
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
  kotlin("multiplatform")
}

spotless {
  kotlin {
    targetExclude(
      // Non-Square licensed files
      "src/*Main/kotlin/io/github/kavikt/ClassName.kt",
      "src/*Main/kotlin/io/github/kavikt/ClassName.*.kt",
      "src/*Test/kotlin/io/github/kavikt/AbstractTypesTest.kt",
      "src/*Test/kotlin/io/github/kavikt/ClassName*Test*.kt",
      "src/*Test/kotlin/io/github/kavikt/TypesEclipseTest.kt",
      "src/*Test/kotlin/io/github/kavikt/TypesTest.kt",
    )
  }
}

kotlin {
  jvm()

  js {
    nodejs {
      testTask {
        useMocha()
      }
    }
    binaries.library()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    nodejs {
      testTask {
        useMocha()
      }
    }
    binaries.library()
  }

  macosArm64()
  linuxX64()
  linuxArm64()
  mingwX64()

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  compilerOptions {
    allWarningsAsErrors = true
    optIn.add("io.github.kavikt.DelicateKaviApi")
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    commonMain {
      dependencies {
        api(libs.okio)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.assertk)
        implementation(libs.okio.fakefilesystem)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.kotlin.reflect)
      }
    }

    jvmTest {
      dependencies {
        implementation(libs.kotlin.junit)
        implementation(libs.assertk)
        implementation(libs.compileTesting)
        implementation(libs.jimfs)
        implementation(libs.ecj)
        implementation(libs.kotlinCompileTesting)
        implementation(libs.kotlin.annotationProcessingEmbeddable)
        implementation(libs.kotlin.compilerEmbeddable)
      }
    }

    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val jvmNativeMain by creating {
      dependsOn(commonMain.get())
    }
    val nonWasmTest by creating {
      dependsOn(commonTest.get())
    }
    val jvmNativeTest by creating {
      dependsOn(nonWasmTest)
    }
    val nativeMain by creating {
      dependsOn(nonJvmMain)
      dependsOn(jvmNativeMain)
    }
    val nativeTest by creating {
      dependsOn(jvmNativeTest)
    }

    jsMain { dependsOn(nonJvmMain) }
    jsTest { dependsOn(nonWasmTest) }

    wasmJsMain {
      languageSettings {
        optIn("kotlin.js.ExperimentalWasmJsInterop")
      }
      dependsOn(nonJvmMain)
    }

    jvmMain { dependsOn(jvmNativeMain) }
    jvmTest { dependsOn(jvmNativeTest) }
    linuxArm64Main { dependsOn(nativeMain) }
    linuxArm64Test { dependsOn(nativeTest) }
    linuxX64Main { dependsOn(nativeMain) }
    linuxX64Test { dependsOn(nativeTest) }
    macosArm64Main { dependsOn(nativeMain) }
    macosArm64Test { dependsOn(nativeTest) }
    mingwX64Main { dependsOn(nativeMain) }
    mingwX64Test { dependsOn(nativeTest) }
  }
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
  manifest {
    attributes("Automatic-Module-Name" to "io.github.kavikt")
  }
}

tasks.named<KotlinCompile>("compileTestKotlinJvm") {
  compilerOptions {
    freeCompilerArgs.add("-opt-in=io.github.kavikt.DelicateKaviApi")
  }
}

tasks.withType<KotlinNativeCompile>().configureEach {
  if (name.endsWith("KotlinMetadata")) {
    compilerOptions.allWarningsAsErrors.set(false)
  }
}
