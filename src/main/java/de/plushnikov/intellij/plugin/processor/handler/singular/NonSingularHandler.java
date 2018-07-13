package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class NonSingularHandler implements BuilderElementHandler {
  private static final String SETTER_PREFIX = "set";

  NonSingularHandler() {
  }

  public LombokLightFieldBuilder renderBuilderField(@NotNull BuilderInfo info) {
    return new LombokLightFieldBuilder(info.getManager(), info.getFieldName(), info.getFieldType())
      .withModifier(PsiModifier.PRIVATE)
      .withNavigationElement(info.getVariable());
  }

  public void addBuilderMethod(@NotNull List<PsiMethod> methods, @NotNull PsiVariable psiVariable, @NotNull String fieldName, @NotNull PsiClass innerClass, boolean fluentBuilder, PsiType returnType, String psiFieldName, PsiSubstitutor builderSubstitutor) {
    methods.add(new LombokLightMethodBuilder(psiVariable.getManager(), createSetterName(psiFieldName, fluentBuilder))
      .withMethodReturnType(returnType)
      .withContainingClass(innerClass)
      .withParameter(psiFieldName, builderSubstitutor.substitute(psiVariable.getType()))
      .withNavigationElement(psiVariable)
      .withModifier(PsiModifier.PUBLIC)
      .withBody(createCodeBlock(innerClass, fluentBuilder, psiFieldName)));
  }

  @Override
  public Collection<PsiMethod> renderBuilderMethod(@NotNull BuilderInfo info) {
    return Collections.singleton(
      new LombokLightMethodBuilder(info.getManager(), createSetterName(info.getFieldName(), info.isFluentBuilder()))
        .withMethodReturnType(info.isChainBuilder() ? info.getBuilderType() : PsiType.VOID)
        .withParameter(info.getFieldName(), info.getFieldType())
        .withNavigationElement(info.getVariable())
        .withModifier(PsiModifier.PUBLIC)
        .withBody(createCodeBlock(info.getBuilderClass(), info.isFluentBuilder(), info.getFieldName())));
  }

  @NotNull
  private PsiCodeBlock createCodeBlock(@NotNull PsiClass innerClass, boolean fluentBuilder, String psiFieldName) {
    final String blockText = getAllMethodBody(psiFieldName, fluentBuilder);
    return PsiMethodUtil.createCodeBlockFromText(blockText, innerClass);
  }

  @Override
  public String createSingularName(PsiAnnotation singularAnnotation, String psiFieldName) {
    return psiFieldName;
  }

  @NotNull
  private String createSetterName(@NotNull String fieldName, boolean isFluent) {
    return isFluent ? fieldName : SETTER_PREFIX + StringUtil.capitalize(fieldName);
  }

  private String getAllMethodBody(@NotNull String psiFieldName, boolean fluentBuilder) {
    final String codeBlockTemplate = "this.{0} = {0};{1}";
    return MessageFormat.format(codeBlockTemplate, psiFieldName, fluentBuilder ? "\nreturn this;" : "");
  }
}
