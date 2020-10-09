package de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import lombok.WithCodeAndDesc;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Inspect and validate @WithCodeAndDesc lombok annotation on a class
 * Creates code, desc fields, corresponding constructors and of() method
 */
public class WithCodeAndDescFieldProcessor extends AbstractWithCodeAndDescProcessor {

  public WithCodeAndDescFieldProcessor() {
    super(PsiField.class, WithCodeAndDesc.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return validateAnnotationOnRightType(psiClass, builder);
  }

  private boolean validateAnnotationOnRightType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    if (!psiClass.isEnum()) {
      builder.addError("@WithCodeAndDesc is only supported on a class or enum type");
      return false;
    }
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    if (!hasFieldByName(psiClass, getAnnotatedValue(psiAnnotation, CODE_FIELD_NAME, "code"))) {
      target.add(createCodeField(psiClass, psiAnnotation));
    }
    if (!hasFieldByName(psiClass, getAnnotatedValue(psiAnnotation, DESC_FIELD_NAME, "desc"))) {
      target.add(createDescField(psiClass, psiAnnotation));
    }
  }

  PsiField createCodeField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final PsiManager psiManager = psiClass.getManager();
    String fieldName = getAnnotatedValue(psiAnnotation, CODE_FIELD_NAME, "code");
    PsiType fieldType = PsiType.INT;

    return new LombokLightFieldBuilder(psiManager, fieldName, fieldType)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PRIVATE);
  }

  PsiField createDescField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final PsiManager psiManager = psiClass.getContainingFile().getManager();
    String fieldName = getAnnotatedValue(psiAnnotation, DESC_FIELD_NAME, "desc");
    PsiType fieldType = PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject()));
    return new LombokLightFieldBuilder(psiManager, fieldName, fieldType)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PRIVATE);
  }

}
