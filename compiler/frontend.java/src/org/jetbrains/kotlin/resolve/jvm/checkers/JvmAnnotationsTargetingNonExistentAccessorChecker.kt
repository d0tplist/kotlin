/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.checkers

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.MemberDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportDiagnosticOnce
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.annotations.hasJvmStaticAnnotation
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.jvm.annotations.hasJvmFieldAnnotation

class JvmAnnotationsTargetingNonExistentAccessorChecker : DeclarationChecker {
    companion object {
        private val getterUselessTargets = setOf(PROPERTY_GETTER)
        private val setterUselessTargets = setOf(PROPERTY_SETTER, SETTER_PARAMETER)
    }

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is MemberDescriptor) return
        if (declaration !is KtParameter && declaration !is KtProperty) return

        if (!Visibilities.isPrivate(descriptor.visibility) && !isSpecialStaticProperty(descriptor)) return

        val hasGetterBody = declaration is KtProperty && declaration.getter?.hasBody() == true
        val hasSetterBody = declaration is KtProperty && declaration.setter?.hasBody() == true

        if (hasGetterBody && hasSetterBody) return
        if (declaration is KtProperty && declaration.hasDelegate()) return

        for (annotation in declaration.annotationEntries) {
            val psiTarget = annotation.useSiteTarget ?: continue
            val useSiteTarget = psiTarget.getAnnotationUseSiteTarget()
            if (!hasGetterBody && useSiteTarget in getterUselessTargets ||
                !hasSetterBody && useSiteTarget in setterUselessTargets
            ) {
                context.trace.reportDiagnosticOnce(Errors.ANNOTATION_IS_TARGETING_NON_EXISTENT_ACCESSOR.on(annotation))
            }
        }

        if (declaration is KtProperty) {
            if (!hasGetterBody) {
                declaration.getter?.annotationEntries?.forEach {
                    context.trace.reportDiagnosticOnce(Errors.ANNOTATION_IS_TARGETING_NON_EXISTENT_ACCESSOR.on(it))
                }
            }

            if (!hasSetterBody) {
                declaration.setter?.annotationEntries?.forEach {
                    context.trace.reportDiagnosticOnce(Errors.ANNOTATION_IS_TARGETING_NON_EXISTENT_ACCESSOR.on(it))
                }
            }
        }

    }

    private fun isSpecialStaticProperty(descriptor: MemberDescriptor): Boolean {
        return descriptor.hasJvmStaticAnnotation() ||
                descriptor.hasJvmFieldAnnotation() ||
                (descriptor is VariableDescriptor && descriptor.isConst)

    }
}
