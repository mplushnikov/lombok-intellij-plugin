package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class NewBuilderHandler {

  @NotNull
  public static Stream<BuilderInfo> createBuilderInfo(@NotNull PsiClass psiClass, @Nullable PsiMethod psiClassMethod) {
    if (null != psiClassMethod) {
      return Arrays.stream(psiClassMethod.getParameterList().getParameters()).map(BuilderInfo::fromPsiParameter);
    } else {
      return PsiClassUtil.collectClassFieldsIntern(psiClass).stream().map(BuilderInfo::fromPsiField)
        .filter(BuilderInfo::useForBuilder);
    }
  }

}
