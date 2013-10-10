package de.plushnikov.intellij.lombok.processor.clazz;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import de.plushnikov.intellij.lombok.problem.ProblemBuilder;
import de.plushnikov.intellij.lombok.problem.ProblemEmptyBuilder;
import de.plushnikov.intellij.lombok.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.lombok.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import de.plushnikov.intellij.lombok.util.PsiClassUtil;
import java.lang.annotation.Annotation;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author twillouer
 */
public class ValueProcessor extends AbstractLombokClassProcessor {

  public ValueProcessor() {
    this(Value.class);
  }

  protected ValueProcessor(@NotNull Class<? extends Annotation> supportedAnnotationClass) {
    super(supportedAnnotationClass, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    validateCallSuperParam(psiAnnotation, psiClass, builder, "equals/hashCode");

    return validateAnnotationOnRigthType(psiClass, builder);
  }

  protected void validateCallSuperParam(PsiAnnotation psiAnnotation, PsiClass psiClass, ProblemBuilder builder, String generatedMethodName) {
    if (PsiAnnotationUtil.isNotAnnotatedWith(psiClass, EqualsAndHashCode.class)) {
      if (PsiClassUtil.hasSuperClass(psiClass)) {
        builder.addWarning("Generating " + generatedMethodName + " implementation but without a call to superclass, " +
            "even though this class does not extend java.lang.Object." +
            "If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.",
            PsiQuickFixFactory.createAddAnnotationQuickFix(psiClass, "lombok.EqualsAndHashCode", "callSuper=false"));
      }
    }
  }

  protected boolean validateAnnotationOnRigthType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addError("'@Data' is only supported on a class type");
      result = false;
    }
    return result;
  }

  protected void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    //@Value class are final
    if(!PsiClassUtil.isFinalClass(psiClass)) {
//      PsiUtil.setModifierProperty(psiClass, PsiModifier.FINAL, true);
    }

    if (PsiAnnotationUtil.isNotAnnotatedWith(psiClass, Getter.class)) {
      target.addAll(new GetterProcessor().createFieldGetters(psiClass, PsiModifier.PUBLIC));
    }
    if (PsiAnnotationUtil.isNotAnnotatedWith(psiClass, EqualsAndHashCode.class)) {
      target.addAll(new EqualsAndHashCodeProcessor().createEqualAndHashCode(psiClass, psiAnnotation, false));
    }
    if (PsiAnnotationUtil.isNotAnnotatedWith(psiClass, ToString.class)) {
      target.addAll(new ToStringProcessor().createToStringMethod(psiClass, psiAnnotation));
    }
    // create required constructor only if there are no other constructor annotations
    if (PsiAnnotationUtil.isNotAnnotatedWith(psiClass, NoArgsConstructor.class, RequiredArgsConstructor.class, AllArgsConstructor.class)) {
      final Collection<PsiMethod> definedConstructors = PsiClassUtil.collectClassConstructorIntern(psiClass);
      // and only if there are no any other constructors!
      if (definedConstructors.isEmpty()) {
        final AllArgsConstructorProcessor allArgsConstructorProcessor = new AllArgsConstructorProcessor();

        final String staticName = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "staticConstructor", String.class);
        final Collection<PsiField> requiredFields = allArgsConstructorProcessor.getAllFields(psiClass);

        if (allArgsConstructorProcessor.validateIsConstructorDefined(psiClass, staticName, requiredFields, ProblemEmptyBuilder.getInstance())) {
          target.addAll(allArgsConstructorProcessor.createAllArgsConstructor(
              psiClass, PsiModifier.PUBLIC, psiAnnotation, staticName));
        }
      }
    }
  }

}
