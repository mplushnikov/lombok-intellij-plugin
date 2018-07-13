package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface BuilderElementHandler {

  void addBuilderMethod(@NotNull List<PsiMethod> methods, @NotNull PsiVariable psiVariable, @NotNull String fieldName, @NotNull PsiClass innerClass, boolean fluentBuilder, PsiType returnType, String singularName, PsiSubstitutor builderSubstitutor);

  String createSingularName(PsiAnnotation singularAnnotation, String psiFieldName);

  default void appendBuildPrepare(@NotNull StringBuilder buildMethodParameters, @NotNull PsiVariable psiVariable, @NotNull String fieldName) {
  }

  default void appendBuildCall(@NotNull StringBuilder buildMethodParameters, @NotNull String fieldName) {
    buildMethodParameters.append(fieldName);
  }

  LombokLightFieldBuilder renderBuilderField(@NotNull BuilderInfo info);

  default LombokLightFieldBuilder renderAdditionalBuilderField(@NotNull BuilderInfo info) {
    return null;
  }

  Collection<PsiMethod> renderBuilderMethod(@NotNull BuilderInfo info);
}
