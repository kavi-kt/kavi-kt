# Contributing

Thanks for your interest in Kavi-KT — contributions are very welcome!

Kavi-KT is a Kotlin Multiplatform fork of [KotlinPoet](https://github.com/square/kotlinpoet) and tracks
it closely. That mostly just affects where a change is the best fit:

- **Core code-generation changes** — a new builder API, emitter behaviour, or a bug that also shows up on
  the JVM — are usually best sent to **KotlinPoet upstream**. We merge upstream regularly, so the fix flows
  down into Kavi-KT and stays shared with everyone.
- **Kavi-KT's own things** are very welcome right here: multiplatform bugs (Native/JS/Wasm), missing target
  support, packaging, and the fork's own glue.

Not sure where something belongs? Feel free to open an issue anyway when you're unsure — we'll sort it out.

## Pull requests

Fork the repo, make your change on a branch, and open a pull request. Keeping changes focused and roughly in
line with the surrounding style helps a lot.

## Formatting and checks

We format with [ktfmt](https://github.com/facebook/ktfmt) (Google style) via Spotless. Before pushing:

```
./gradlew spotlessApply   # apply formatting
./gradlew check           # tests + API checks
```
