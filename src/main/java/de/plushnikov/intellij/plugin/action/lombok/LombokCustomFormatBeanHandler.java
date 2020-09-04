package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.Convertable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author lihongbin
 */
public class LombokCustomFormatBeanHandler extends BaseLombokHandler {


  protected void processClass(@NotNull PsiClass psiClass) {
    delToBeanMethod(psiClass);
    delFormBeanMethod(psiClass);
    addAnnotation(psiClass, Convertable.class);
  }

  private void delFormBeanMethod(@NotNull PsiClass psiClass) {
    for (PsiMethod method : getFromBeanMethod(psiClass)) {
      method.delete();
    }
  }


  private void delToBeanMethod(@NotNull PsiClass psiClass) {
    for (PsiMethod method : getToBeanJsonMethod(psiClass)) {
      method.delete();
    }
  }

  private List<PsiMethod> getFromBeanMethod(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.findMethodsByName("fromBean", false);
    return Arrays.stream(methods)
      .filter(x -> x.getParameters().length == 1).collect(toList());
  }

  private List<PsiMethod> getToBeanJsonMethod(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.findMethodsByName("toBean", false);
    return Arrays.stream(methods)
      .filter(x -> x.getParameters().length == 1).collect(toList());
  }
}
