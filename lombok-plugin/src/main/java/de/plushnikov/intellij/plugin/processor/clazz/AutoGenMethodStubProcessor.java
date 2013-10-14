package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.infos.CandidateInfo;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.thirdparty.ErrorMessages;
import lombok.AutoGenMethodStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @AutoGenMethodStub lombok-pg annotation on a class
 * Creates methods for all of unimplemented methods of the annotated type and create method stubs for all of them
 * TODO implement me
 * @author Plushnikov Michail
 */
public class AutoGenMethodStubProcessor extends AbstractClassProcessor {
  public AutoGenMethodStubProcessor() {
    super(AutoGenMethodStub.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return validateAnnotationOnRightType(psiClass, builder);
  }

  protected boolean validateAnnotationOnRightType(@NotNull final PsiClass psiClass, @NotNull final ProblemBuilder builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addError(ErrorMessages.canBeUsedOnClassAndEnumOnly(getSupportedAnnotationClass()));
      result = false;
    }
    return result;
  }

  protected void processIntern(@NotNull final PsiClass psiClass, @NotNull final PsiAnnotation psiAnnotation, @NotNull final List<? super PsiElement> target) {
    //todo reimplement this to get it working
    final Collection<CandidateInfo> candidateInfos = OverrideImplementUtil.getMethodsToOverrideImplement(psiClass, true);
    for (CandidateInfo candidateInfo : candidateInfos) {

      PsiMethod implementedMethod = createImplementingMethod(candidateInfo, psiClass, psiAnnotation);

      if (null != implementedMethod) {
        target.add(implementedMethod);
      }
    }
  }

  private PsiMethod createImplementingMethod(CandidateInfo candidateInfo, PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final PsiMethod methodToImplement = (PsiMethod) candidateInfo.getElement();
    final PsiSubstitutor substitutor = candidateInfo.getSubstitutor();
    if (null != methodToImplement && null != substitutor) {
      final String methodName = methodToImplement.getName();
      LombokLightMethodBuilder method = new LombokLightMethodBuilder(psiClass.getManager(), methodName)
          .withMethodReturnType(substitutor.substitute(methodToImplement.getReturnType()))
          .withContainingClass(psiClass)
          .withNavigationElement(psiAnnotation);
      addModifier(methodToImplement, method, PsiModifier.PUBLIC);
      addModifier(methodToImplement, method, PsiModifier.PACKAGE_LOCAL);
      addModifier(methodToImplement, method, PsiModifier.PROTECTED);

      for (PsiParameter psiParameter : methodToImplement.getParameterList().getParameters()) {
        method.withParameter(psiParameter.getName(), substitutor.substitute(psiParameter.getType()));
      }

      for (PsiClassType psiClassType : methodToImplement.getThrowsList().getReferencedTypes()) {
        method.withException(psiClassType);
      }

      return method;
    }
    return null;
  }

  private void addModifier(PsiMethod psiMethod, LombokLightMethodBuilder method, String modifier) {
    if (psiMethod.hasModifierProperty(modifier)) {
      method.withModifier(modifier);
    }
  }

}
