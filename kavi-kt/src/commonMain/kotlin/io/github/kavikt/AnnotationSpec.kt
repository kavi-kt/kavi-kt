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

import kotlin.LazyThreadSafetyMode.NONE
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/** A generated annotation on a declaration. */
public class AnnotationSpec
private constructor(builder: Builder, private val tagMap: TagMap = builder.buildTagMap()) :
  Taggable by tagMap {
  @Deprecated(
    message = "Use typeName instead. This property will be removed in Kavi 2.0.",
    replaceWith = ReplaceWith("typeName"),
  )
  public val className: ClassName
    get() = typeName as? ClassName ?: error("ClassName is not available. Call typeName instead.")

  public val typeName: TypeName = builder.typeName
  public val members: List<CodeBlock> = builder.members.toImmutableList()
  public val useSiteTarget: UseSiteTarget? = builder.useSiteTarget

  /** Lazily-initialized toString of this AnnotationSpec. */
  private val cachedString by
    lazy(NONE) { buildCodeString { emit(this, inline = true, asParameter = false) } }

  internal fun emit(codeWriter: CodeWriter, inline: Boolean, asParameter: Boolean = false) {
    if (!asParameter) {
      codeWriter.emit("@")
    }
    if (useSiteTarget != null) {
      codeWriter.emit(useSiteTarget.keyword + ":")
    }
    codeWriter.emitCode("%T", typeName)

    if (members.isEmpty() && !asParameter) {
      // @Singleton
      return
    }

    val whitespace = if (inline) "" else "\n"
    val memberSeparator = if (inline) ", " else ",\n"
    val memberSuffix = if (!inline && members.size > 1) "," else ""

    // Inline:
    //   @Column(name = "updated_at", nullable = false)
    //
    // Not inline:
    //   @Column(
    //       name = "updated_at",
    //       nullable = false,
    //   )

    codeWriter.emit("(")
    if (members.size > 1) codeWriter.emit(whitespace).indent(1)
    codeWriter.emitCode(
      codeBlock =
        members
          .map { if (inline) it.replaceAll("[⇥|⇤]", "") else it }
          .joinToCode(separator = memberSeparator, suffix = memberSuffix),
      isConstantContext = true,
    )
    if (members.size > 1) codeWriter.unindent(1).emit(whitespace)
    codeWriter.emit(")")
  }

  public fun toBuilder(): Builder {
    val builder = Builder(typeName)
    builder.members += members
    builder.useSiteTarget = useSiteTarget
    builder.tags += tagMap.tags
    return builder
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (this::class != other::class) return false
    return toString() == other.toString()
  }

  override fun hashCode(): Int = toString().hashCode()

  override fun toString(): String = cachedString

  public enum class UseSiteTarget(internal val keyword: String) {
    FILE("file"),
    PROPERTY("property"),
    FIELD("field"),
    GET("get"),
    SET("set"),
    RECEIVER("receiver"),
    PARAM("param"),
    SETPARAM("setparam"),
    DELEGATE("delegate"),
    @ExperimentalKaviApi ALL("all"),
  }

  public class Builder internal constructor(internal val typeName: TypeName) :
    Taggable.Builder<Builder> {
    internal var useSiteTarget: UseSiteTarget? = null

    public val members: MutableList<CodeBlock> = mutableListOf()
    override val tags: MutableMap<KClass<*>, Any> = mutableMapOf()

    public fun addMember(format: String, vararg args: Any): Builder =
      addMember(CodeBlock.of(format, *args))

    public fun addMember(codeBlock: CodeBlock): Builder = apply { members += codeBlock }

    public fun useSiteTarget(useSiteTarget: UseSiteTarget?): Builder = apply {
      this.useSiteTarget = useSiteTarget
    }

    public fun build(): AnnotationSpec = AnnotationSpec(this)

    public companion object
  }

  public companion object {
    @JvmStatic public fun builder(type: ClassName): Builder = Builder(type)

    @JvmStatic public fun builder(type: ParameterizedTypeName): Builder = Builder(type)
  }
}
