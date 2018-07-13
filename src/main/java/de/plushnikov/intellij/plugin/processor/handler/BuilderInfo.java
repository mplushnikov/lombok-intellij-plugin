package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.processor.handler.singular.BuilderElementHandler;
import de.plushnikov.intellij.plugin.processor.handler.singular.SingularHandlerFactory;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class BuilderInfo {
  private PsiVariable variableInClass;
  private PsiType fieldInBuilderType;

  private PsiClass builderClass;
  private PsiType builderClassType;

  private String fieldInBuilderName;
  private PsiExpression fieldInitializer;
  private boolean hasBuilderDefaultAnnotation;

  private PsiAnnotation singularAnnotation;
  private BuilderElementHandler builderElementHandler;

  private boolean fluentBuilder = true;
  private boolean chainBuilder = true;

  public static BuilderInfo fromPsiParameter(@NotNull PsiParameter psiParameter) {
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

  public BuilderInfo withSubstitutor(PsiSubstitutor builderSubstitutor) {
    fieldInBuilderType = builderSubstitutor.substitute(fieldInBuilderType);
    return this;
  }

  public BuilderInfo withFluent(boolean fluentBuilder) {
    this.fluentBuilder = fluentBuilder;
    return this;
  }

  public BuilderInfo withChain(boolean chainBuilder) {
    this.chainBuilder = chainBuilder;
    return this;
  }

  public BuilderInfo withBuilderClass(@NotNull PsiClass builderClass) {
    this.builderClass = builderClass;
    this.builderClassType = PsiClassUtil.getTypeWithGenerics(builderClass);
    return this;
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

  public boolean notAlreadyExistingField(Collection<String> alreadyExistingFieldNames) {
    return !alreadyExistingFieldNames.contains(fieldInBuilderName);
  }

  public boolean notAlreadyExistingMethod(Collection<String> existedMethodNames) {
    return notAlreadyExistingField(existedMethodNames);
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

  public boolean isFluentBuilder() {
    return fluentBuilder;
  }

  public boolean isChainBuilder() {
    return chainBuilder;
  }

  public PsiClass getBuilderClass() {
    return builderClass;
  }

  public PsiType getBuilderType() {
    return builderClassType;
  }

  public PsiAnnotation getSingularAnnotation() {
    return singularAnnotation;
  }


  public Collection<PsiField> renderBuilderFields() {
    return builderElementHandler.renderBuilderFields(this);
  }

  public Collection<PsiMethod> renderBuilderMethods() {
    return builderElementHandler.renderBuilderMethod(this);
  }

  public String renderBuildPrepare() {
    return builderElementHandler.renderBuildPrepare(variableInClass, fieldInBuilderName);
  }

  public String renderBuildCall() {
    return fieldInBuilderName;
  }
}
