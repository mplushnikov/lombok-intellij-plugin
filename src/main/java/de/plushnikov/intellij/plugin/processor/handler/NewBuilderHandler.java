package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class NewBuilderHandler {

  @NotNull
  public static Stream<BuilderInfo> createBuilderInfo(@NotNull PsiClass psiClass) {
    return PsiClassUtil.collectClassFieldsIntern(psiClass).stream()
      .map(BuilderInfo::fromPsiField)
      .filter(BuilderInfo::useForBuilder);
  }

  @NotNull
  public static Stream<BuilderInfo> createBuilderInfo(@NotNull PsiMethod psiMethod) {
    return Arrays.stream(psiMethod.getParameterList().getParameters())
      .map(BuilderInfo::fromPsiParameter);
  }
}
