package de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

public abstract class AbstractWithCodeAndDescProcessor extends AbstractClassProcessor {
  static final String CODE_FIELD_NAME = "codeName";
  static final String DESC_FIELD_NAME = "descName";

  protected AbstractWithCodeAndDescProcessor(@NotNull Class<? extends PsiElement> supportedClass, @NotNull Class<? extends Annotation> supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  protected String getAnnotatedValue(@NotNull PsiAnnotation psiAnnotation, @NotNull String attrName, @NotNull String defaultName) {
    if (PsiAnnotationUtil.hasDeclaredProperty(psiAnnotation, attrName)) {
      return PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, attrName);
    } else {
      return defaultName;
    }
  }

  protected boolean hasFieldByName(@NotNull PsiClass psiClass, @NotNull String fieldName) {
    final Collection<PsiField> psiFields = PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : psiFields) {
      if (fieldName.equals(psiField.getName())) {
        return true;
      }
    }
    return false;
  }

  protected PsiField findCodeField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    String fieldName = this.getAnnotatedValue(psiAnnotation, CODE_FIELD_NAME, "code");
    final Collection<PsiField> psiFields = PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : psiFields) {
      if (fieldName.equals(psiField.getName())) {
        return psiField;
      }
    }
    return ServiceManager.getService(WithCodeAndDescFieldProcessor.class).createCodeField(psiClass, psiAnnotation);
  }

  protected PsiField findDescField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    String fieldName = this.getAnnotatedValue(psiAnnotation, DESC_FIELD_NAME, "desc");
    final Collection<PsiField> psiFields = PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : psiFields) {
      if (fieldName.equals(psiField.getName())) {
        return psiField;
      }
    }
    return ServiceManager.getService(WithCodeAndDescFieldProcessor.class).createDescField(psiClass, psiAnnotation);
  }

  private GetterFieldProcessor getGetterFieldProcessor() {
    return ServiceManager.getService(GetterFieldProcessor.class);
  }
}
