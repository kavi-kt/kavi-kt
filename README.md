Kavi-KT
=======

`Kavi-KT` is a Kotlin Multiplatform API for generating `.kt` source files.

> *Kavi* (कवि) is Sanskrit for "poet", so *Kavi-KT* reads as "Kotlin poet" — the same KotlinPoet, only slightly different.

It is a fork of [Square's KotlinPoet][upstream] with a portable `commonMain` core: the full
code-generation API runs on the JVM, Kotlin/Native, Kotlin/JS, and Kotlin/Wasm. JVM-only interop
(`javax.lang.model`, reflection, kotlin-metadata) remains available on the JVM target.

[![Maven Central][version-shield]][maven-central]
[![License][license-shield]][license]

## Example

Generate a `HelloWorld.kt` file:

```kotlin
val file = FileSpec.builder("com.example", "HelloWorld")
  .addFunction(
    FunSpec.builder("main")
      .addStatement("println(%S)", "Hello, Kavi-KT!")
      .build()
  )
  .build()

println(file)
```

The code above prints:

```kotlin
package com.example

public fun main() {
  println("Hello, Kavi-KT!")
}
```

## Download

```kotlin
dependencies {
  // Multiplatform code-generation core.
  implementation("io.github.kavi-kt:kavi-kt:2.4.0-alpha01")

  // Optional JVM-only interop modules:
  implementation("io.github.kavi-kt:kavi-kt-metadata:2.4.0-alpha01")
  implementation("io.github.kavi-kt:kavi-kt-ksp:2.4.0-alpha01")
}
```

## Platform support

The core artifact works from `commonMain` in a multiplatform project and from a plain JVM project
alike — Gradle module metadata resolves the right variant per target. The `-metadata` and `-ksp`
interop modules are JVM-only. [okio][okio] is exposed as an `api` dependency of the core for
multiplatform file I/O.

| Target | Notes |
| --- | --- |
| JVM | Full API, including `javax.lang.model` / reflection / kotlin-metadata interop |
| Kotlin/Native | `macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64` |
| Kotlin/JS | Node and browser |
| Kotlin/Wasm | `wasmJs` |

The emitter is fully functional on every target. Annotation-processing inputs (`javax.lang.model`)
and `java.lang.reflect` are JVM-only; Kotlin reflection (`KClass` / `KType` → `TypeName`) works on the
JVM and Native but not JS/Wasm; off-JVM file output goes through `okio`. The table below breaks the
API down by target.

| API | JVM | Native | JS / Wasm |
| --- | :---: | :---: | :---: |
| Core specs & emitter (all builders, `CodeWriter`, `FileSpec`, `CodeBlock`) | ✅ | ✅ | ✅ |
| Structural type model (`ClassName`, `ParameterizedTypeName`, … from strings) | ✅ | ✅ | ✅ |
| File output via `okio` | ✅ | ✅ | ✅ |
| File output via `java.nio` / `Filer` / `JavaFileObject` | ✅ | — | — |
| `KClass` reflection → `TypeName` (`asClassName`, `typeNameOf`, builder overloads) | ✅ | ✅ | — |
| `KType` reflection → `TypeName` | ✅ | 🟡 | — |
| `javax.lang.model` & `java.lang.reflect` interop (KSP/KAPT) | ✅ | — | — |
| Identifier escaping / Unicode | ✅ | 🟡 | 🟡 |
| Number literals (`%L`) | ✅ | ✅ | 🟡 |

✅ full · 🟡 minor documented edge-case · — not available on this target

Edge-cases:

- **Native** — `KType` reflection over **nested** generic classes (e.g. `Map.Entry<K, V>`) throws /
  returns null; build the type structurally instead.
- **Native / JS / Wasm** — identifiers with astral (non-BMP) letters fall back to `_`.
- **JS / Wasm** — an integral `Double`/`Float` in `%L` prints without a trailing `.0`.

The structural API is unaffected on all targets.

## Documentation

The KotlinPoet [guides][upstream-docs] apply — the API is identical aside from the package name and
annotations.

## Relationship to KotlinPoet

Kavi-KT keeps the KotlinPoet API shape; migrating an existing project is a source-level change:

- depend on `io.github.kavi-kt:kavi-kt` instead of `com.squareup:kotlinpoet`,
- replace the `com.squareup.kotlinpoet` package prefix with `io.github.kavikt` in imports
  (`import io.github.kavikt.*`),
- rename the opt-in annotations: `@DelicateKotlinPoetApi` → `@DelicateKaviApi`,
  `@ExperimentalKotlinPoetApi` → `@ExperimentalKaviApi`.

License
-------

    Copyright 2017 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Kavi-KT is a fork of [KotlinPoet][upstream], and is distributed under the same Apache 2.0 license.

## Acknowledgements

A huge thank-you to [ForteScarlet][forte] for the enormous effort that went into the original Kotlin
Multiplatform migration of KotlinPoet — Kavi-KT stands on that work. And thank you to Square and every
KotlinPoet contributor for building such a great tool in the first place.

 [upstream]: https://github.com/square/kotlinpoet
 [upstream-docs]: https://square.github.io/kotlinpoet/
 [javadoc]: https://javadoc.io/doc/io.github.kavi-kt/kavi-kt
 [okio]: https://square.github.io/okio/
 [maven-central]: https://central.sonatype.com/artifact/io.github.kavi-kt/kavi-kt
 [version-shield]: https://img.shields.io/maven-central/v/io.github.kavi-kt/kavi-kt
 [license]: https://www.apache.org/licenses/LICENSE-2.0
 [license-shield]: https://img.shields.io/badge/license-Apache%202.0-blue
 [forte]: https://github.com/ForteScarlet
