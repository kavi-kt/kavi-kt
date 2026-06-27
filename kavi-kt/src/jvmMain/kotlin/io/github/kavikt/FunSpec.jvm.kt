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
@file:JvmName("FunSpecs")
@file:JvmMultifileClass

package io.github.kavikt

import io.github.kavikt.KModifier.VARARG
import java.lang.reflect.Type
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeVariable
import javax.lang.model.util.Types

/**
 * Sets the JVM modifiers in `modifiers` on this builder, translating each to the corresponding
 * Kotlin modifier or `kotlin.jvm` annotation.
 */
@Suppress("DEPRECATION", "DEPRECATION_ERROR")
public fun FunSpec.Builder.jvmModifiers(modifiers: Iterable<Modifier>) {
  var visibility = KModifier.INTERNAL
  for (modifier in modifiers) {
    when (modifier) {
      Modifier.PUBLIC -> visibility = KModifier.PUBLIC
      Modifier.PROTECTED -> visibility = KModifier.PROTECTED
      Modifier.PRIVATE -> visibility = KModifier.PRIVATE
      Modifier.ABSTRACT -> this.modifiers += KModifier.ABSTRACT
      Modifier.FINAL -> this.modifiers += KModifier.FINAL
      Modifier.NATIVE -> this.modifiers += KModifier.EXTERNAL
      Modifier.DEFAULT -> Unit
      Modifier.STATIC -> addAnnotation(ClassName("kotlin.jvm", "JvmStatic"))
      Modifier.SYNCHRONIZED -> addAnnotation(ClassName("kotlin.jvm", "Synchronized"))
      Modifier.STRICTFP -> addAnnotation(ClassName("kotlin.jvm", "Strictfp"))
      else -> throw IllegalArgumentException("unexpected fun modifier $modifier")
    }
  }
  this.modifiers += visibility
}

/** Sets the receiver of this builder to `receiverType`, with optional `kdoc`. */
@JvmOverloads
public fun FunSpec.Builder.receiver(
  receiverType: Type,
  kdoc: CodeBlock = CodeBlock.EMPTY,
): FunSpec.Builder = receiver(receiverType.asTypeName(), kdoc)

/** Sets the receiver of this builder to `receiverType`, with a formatted `kdoc`. */
public fun FunSpec.Builder.receiver(
  receiverType: Type,
  kdoc: String,
  vararg args: Any,
): FunSpec.Builder = receiver(receiverType, CodeBlock.of(kdoc, args))

/** Sets the return type of this builder to `returnType`, with optional `kdoc`. */
@JvmOverloads
public fun FunSpec.Builder.returns(
  returnType: Type,
  kdoc: CodeBlock = CodeBlock.EMPTY,
): FunSpec.Builder = returns(returnType.asTypeName(), kdoc)

/** Sets the return type of this builder to `returnType`, with a formatted `kdoc`. */
public fun FunSpec.Builder.returns(
  returnType: Type,
  kdoc: String,
  vararg args: Any,
): FunSpec.Builder = returns(returnType.asTypeName(), CodeBlock.of(kdoc, args))

/** Adds a parameter of the given `type` to this builder. */
public fun FunSpec.Builder.addParameter(
  name: String,
  type: Type,
  vararg modifiers: KModifier,
): FunSpec.Builder = addParameter(name, type.asTypeName(), *modifiers)

/** Adds a parameter of the given `type` to this builder. */
public fun FunSpec.Builder.addParameter(
  name: String,
  type: Type,
  modifiers: Iterable<KModifier>,
): FunSpec.Builder = addParameter(name, type.asTypeName(), modifiers)

/** Returns a [FunSpec.Builder] that overrides `method`. */
@OptIn(DelicateKaviApi::class)
@DelicateKaviApi(
  message =
    "Element APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead."
)
public fun FunSpec.Companion.overriding(method: ExecutableElement): FunSpec.Builder {
  var modifiers: Set<Modifier> = method.modifiers
  require(
    Modifier.PRIVATE !in modifiers && Modifier.FINAL !in modifiers && Modifier.STATIC !in modifiers
  ) {
    "cannot override method with modifiers: $modifiers"
  }

  val methodName = method.simpleName.toString()
  val funBuilder = FunSpec.builder(methodName)

  funBuilder.addModifiers(KModifier.OVERRIDE)

  modifiers = modifiers.toMutableSet()
  modifiers.remove(Modifier.ABSTRACT)
  funBuilder.jvmModifiers(modifiers)

  method.typeParameters
    .map { it.asType() as TypeVariable }
    .map { it.asTypeVariableName() }
    .forEach { funBuilder.addTypeVariable(it) }

  funBuilder.returns(method.returnType.asTypeName())
  funBuilder.addParameters(ParameterSpec.parametersOf(method))
  if (method.isVarArgs) {
    funBuilder.parameters[funBuilder.parameters.lastIndex] =
      funBuilder.parameters.last().toBuilder().addModifiers(VARARG).build()
  }

  if (method.thrownTypes.isNotEmpty()) {
    val throwsValueString = method.thrownTypes.joinToString { "%T::class" }
    funBuilder.addAnnotation(
      AnnotationSpec.builder(Throws::class)
        .addMember(throwsValueString, *method.thrownTypes.toTypedArray())
        .build()
    )
  }

  return funBuilder
}

@OptIn(DelicateKaviApi::class)
@Deprecated(
  message =
    "Element APIs don't give complete information on Kotlin types. Consider using" +
      " the kavi-kt-metadata APIs instead.",
  level = DeprecationLevel.WARNING,
)
public fun FunSpec.Companion.overriding(
  method: ExecutableElement,
  enclosing: DeclaredType,
  types: Types,
): FunSpec.Builder {
  val executableType = types.asMemberOf(enclosing, method) as ExecutableType
  val resolvedParameterTypes = executableType.parameterTypes
  val resolvedReturnType = executableType.returnType

  val builder = overriding(method)
  builder.returns(resolvedReturnType.asTypeName())
  var i = 0
  val size = builder.parameters.size
  while (i < size) {
    val parameter = builder.parameters[i]
    val type = resolvedParameterTypes[i].asTypeName()
    builder.parameters[i] = parameter.toBuilder(parameter.name, type).build()
    i++
  }

  return builder
}
