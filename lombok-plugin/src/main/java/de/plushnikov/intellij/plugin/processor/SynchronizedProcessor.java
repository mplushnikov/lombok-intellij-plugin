package de.plushnikov.intellij.plugin.processor;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.extension.UserMapKeys;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemNewBuilder;
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Inspect and validate @Synchronized lombok annotation
 *
 * @author Plushnikov Michail
 */
public class SynchronizedProcessor extends AbstractProcessor {

  public SynchronizedProcessor() {
    super(Synchronized.class, PsiElement.class);
  }

  @Override
  public Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation) {
    Collection<LombokProblem> result = new ArrayList<LombokProblem>(2);

    final ProblemNewBuilder problemNewBuilder = new ProblemNewBuilder(result);

    PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiAnnotation, PsiMethod.class);
    if (null != psiMethod) {
      if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
        problemNewBuilder.addError("'@Synchronized' is legal only on concrete methods.",
            PsiQuickFixFactory.createModifierListFix(psiMethod, PsiModifier.ABSTRACT, false, false)
        );
      }

      final String lockFieldName = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "value", String.class);
      if (StringUtil.isNotEmpty(lockFieldName)) {
        final PsiClass containingClass = psiMethod.getContainingClass();

        if (null != containingClass) {
          final PsiField lockField = containingClass.findFieldByName(lockFieldName, true);
          if (null != lockField) {
            if (!lockField.hasModifierProperty(PsiModifier.FINAL)) {
              problemNewBuilder.addWarning(String.format("Synchronization on a non-final field %s.", lockFieldName),
                  PsiQuickFixFactory.createModifierListFix(lockField, PsiModifier.FINAL, true, false));
            }
            UserMapKeys.addReadUsageFor(lockField);
          } else {
            final PsiClassType javaLangObjectType = PsiType.getJavaLangObject(containingClass.getManager(), GlobalSearchScope.allScope(containingClass.getProject()));

            problemNewBuilder.addError(String.format("The field %s does not exist.", lockFieldName),
                PsiQuickFixFactory.createNewFieldFix(containingClass, lockFieldName, javaLangObjectType, "new Object()", PsiModifier.PRIVATE, PsiModifier.FINAL));
          }
        }
      }
    } else {
      problemNewBuilder.addError("'@Synchronized' is legal only on methods.");
    }

    return result;
  }
}
