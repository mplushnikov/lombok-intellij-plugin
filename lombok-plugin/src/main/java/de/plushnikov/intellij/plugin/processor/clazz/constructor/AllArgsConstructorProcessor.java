package de.plushnikov.intellij.plugin.processor.clazz.constructor;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
public class AllArgsConstructorProcessor extends AbstractConstructorClassProcessor {

  public AllArgsConstructorProcessor() {
    super(AllArgsConstructor.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result;

    result = super.validate(psiAnnotation, psiClass, builder);

    final Collection<PsiField> allNotInitializedNotStaticFields = getAllNotInitializedAndNotStaticFields(psiClass);
    final String staticConstructorName = getStaticConstructorName(psiAnnotation);
    if (!validateIsConstructorDefined(psiClass, staticConstructorName, allNotInitializedNotStaticFields, builder)) {
      result = false;
    }
    return result;
  }

  protected void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getAccessVisibity(psiAnnotation);
    if (null != methodVisibility) {
      final Collection<PsiField> allNotInitializedNotStaticFields = getAllNotInitializedAndNotStaticFields(psiClass);

      target.addAll(createConstructorMethod(psiClass, methodVisibility, psiAnnotation, allNotInitializedNotStaticFields));
    }
  }

}
