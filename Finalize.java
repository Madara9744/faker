/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.node;

import static com.github.benmanes.caffeine.cache.Specifications.LOOKUP;
import static com.github.benmanes.caffeine.cache.Specifications.METHOD_HANDLES;

import org.apache.commons.lang3.StringUtils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;

/**
 * Finishes construction of the node.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public class Finalize extends NodeRule {

  @Override
  protected boolean applies() {
    return true;
  }

  @Override
  protected void execute() {
    if (!context.suppressedWarnings.isEmpty()) {
      var format = "{" + StringUtils.repeat("$S", ", ", context.suppressedWarnings.size()) + "}";
      context.nodeSubtype.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
          .addMember("value", format, context.suppressedWarnings.toArray())
          .build());
    }
    context.nodeSubtype
        .addMethod(context.constructorDefault.build())
        .addMethod(context.constructorByKey.build())
        .addMethod(context.constructorByKeyRef.build());
    addStaticBlock();
  }

  private void addStaticBlock() {
    if (context.varHandles.isEmpty()) {
      return;
    }
    var codeBlock = CodeBlock.builder()
        .addStatement("$T lookup = $T.lookup()", LOOKUP, METHOD_HANDLES)
        .beginControlFlow("try");
    for (var varHandle : context.varHandles) {
      varHandle.accept(codeBlock);
    }
    codeBlock
        .nextControlFlow("catch ($T e)", ReflectiveOperationException.class)
          .addStatement("throw new ExceptionInInitializerError(e)")
        .endControlFlow();
    context.nodeSubtype.addStaticBlock(codeBlock.build());
  }
}
