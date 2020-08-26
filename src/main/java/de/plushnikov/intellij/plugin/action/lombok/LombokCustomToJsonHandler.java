package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author lihongbin
 */
public class LombokCustomToJsonHandler extends BaseLombokHandler {


  protected void processClass(@NotNull PsiClass psiClass) {
    delToJsonMethod(psiClass);
    delFormJsonMethod(psiClass);
    addAnnotation(psiClass, JsonSerializable.class);
  }

  private void delFormJsonMethod(@NotNull PsiClass psiClass) {
    for (PsiMethod method : getFromJsonMethod(psiClass)) {
      method.delete();
    }
  }


  private void delToJsonMethod(@NotNull PsiClass psiClass) {
    for (PsiMethod method : getToJsonJsonMethod(psiClass)) {
      method.delete();
    }
  }

  private PsiMethod[] getFromJsonMethod(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.findMethodsByName("fromJson", false);
    return (PsiMethod[]) Arrays.stream(methods)
      .filter(x -> x.getParameters().length == 0).toArray();
  }

  private PsiMethod[] getToJsonJsonMethod(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.findMethodsByName("toJson", false);
    return (PsiMethod[]) Arrays.stream(methods)
      .filter(x -> x.getParameters().length == 0).toArray();
  }
}
