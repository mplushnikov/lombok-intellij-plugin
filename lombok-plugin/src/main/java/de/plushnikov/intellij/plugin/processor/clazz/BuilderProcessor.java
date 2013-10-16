package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiTypesUtil;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.thirdparty.ErrorMessages;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.Builder;
import lombok.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @Builder lombok-pg annotation on a class
 * Creates methods for a builder pattern for initializing a class
 * TODO implement me
 *
 * @author Plushnikov Michail
 */
public class BuilderProcessor extends AbstractClassProcessor {

  public static final String METHOD_NAME = "getInstance";

  public BuilderProcessor() {
    super(Builder.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result = validateAnnotationOnRigthType(psiClass, builder);
    if (result) {
      result = validateExistingMethods(psiClass, builder);
    }

    if (PsiClassUtil.hasSuperClass(psiClass)) {
      builder.addError(ErrorMessages.canBeUsedOnConcreteClassOnly(Singleton.class));
      result = false;
    }
    if (PsiClassUtil.hasMultiArgumentConstructor(psiClass)) {
      builder.addError(ErrorMessages.requiresDefaultOrNoArgumentConstructor(Singleton.class));
      result = false;
    }

    return result;
  }

  protected boolean validateAnnotationOnRigthType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addError(ErrorMessages.canBeUsedOnClassOnly(Singleton.class));
      result = false;
    }
    return result;
  }

  protected boolean validateExistingMethods(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result = true;

    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, METHOD_NAME)) {
      builder.addWarning(String.format("Not generated '%s'(): A method with same name already exists", METHOD_NAME));
      result = false;
    }

    return result;
  }

  protected void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    LombokLightMethodBuilder method = new LombokLightMethodBuilder(psiClass.getManager(), METHOD_NAME)
        .withMethodReturnType(PsiTypesUtil.getClassType(psiClass))
        .withContainingClass(psiClass)
        .withNavigationElement(psiAnnotation);
    method.withModifier(PsiModifier.STATIC);
    method.withModifier(PsiModifier.PUBLIC);

    target.add(method);
  }
}
