package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.processor.handler.singular.BuilderElementHandler;
import de.plushnikov.intellij.plugin.processor.handler.singular.SingularHandlerFactory;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BuilderInfo {
  private PsiVariable variableInClass;
  private PsiType fieldInBuilderType;

  private String fieldInBuilderName;
  private PsiExpression fieldInitializer;
  private boolean hasBuilderDefaultAnnotation;

  private PsiAnnotation singularAnnotation;
  private BuilderElementHandler builderElementHandler;

  public static BuilderInfo fromPsiParameter(PsiParameter psiParameter) {
    final BuilderInfo result = new BuilderInfo();

    result.variableInClass = psiParameter;
    result.fieldInBuilderType = psiParameter.getType();
    result.fieldInitializer = null;
    result.hasBuilderDefaultAnnotation = false;

    result.fieldInBuilderName = psiParameter.getName();

    result.singularAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiParameter, Singular.class);
    result.builderElementHandler = SingularHandlerFactory.getHandlerFor(psiParameter, result.singularAnnotation);

    return result;
  }

  public static BuilderInfo fromPsiField(@NotNull PsiField psiField) {
    final BuilderInfo result = new BuilderInfo();

    result.variableInClass = psiField;
    result.fieldInBuilderType = psiField.getType();
    result.fieldInitializer = psiField.getInitializer();
    result.hasBuilderDefaultAnnotation = null == PsiAnnotationSearchUtil.findAnnotation(psiField, BuilderHandler.BUILDER_DEFAULT_ANNOTATION);

    final AccessorsInfo accessorsInfo = AccessorsInfo.build(psiField);
    result.fieldInBuilderName = accessorsInfo.removePrefix(psiField.getName());

    result.singularAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, Singular.class);
    result.builderElementHandler = SingularHandlerFactory.getHandlerFor(psiField, result.singularAnnotation);

    return result;
  }

  public boolean useForBuilder() {
    boolean result = true;

    PsiModifierList modifierList = variableInClass.getModifierList();
    if (null != modifierList) {
      //Skip static fields.
      result = !modifierList.hasModifierProperty(PsiModifier.STATIC);

      // skip initialized final fields unless annotated with @Builder.Default
      final boolean isInitializedFinalField = null != fieldInitializer && modifierList.hasModifierProperty(PsiModifier.FINAL);
      if (isInitializedFinalField && hasBuilderDefaultAnnotation) {
        result = false;
      }
    }

    //Skip fields that start with $
    result &= !fieldInBuilderName.startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);

    return result;
  }

  public Project getProject() {
    return variableInClass.getProject();
  }

  public PsiManager getManager() {
    return variableInClass.getManager();
  }

  public String getFieldName() {
    return fieldInBuilderName;
  }

  public PsiType getFieldType() {
    return fieldInBuilderType;
  }

  public PsiElement getVariable() {
    return variableInClass;
  }

  public BuilderInfo withSubstitutor(PsiSubstitutor builderSubstitutor) {
    fieldInBuilderType = builderSubstitutor.substitute(fieldInBuilderType);
    return this;
  }

  /////////////////////

  public Collection<PsiField> renderBuilderFields() {
    final Collection<PsiField> result;

    final LombokLightFieldBuilder mainBuilderField = builderElementHandler.renderBuilderField(this);
    final LombokLightFieldBuilder additionalBuilderField = builderElementHandler.renderAdditionalBuilderField(this);
    if (null != mainBuilderField) {
      if (null != additionalBuilderField) {
        result = Arrays.asList(mainBuilderField, additionalBuilderField);
      } else {
        result = Collections.singletonList(mainBuilderField);
      }
    } else {
      result = Collections.emptyList();
    }

    return result;
  }

}
