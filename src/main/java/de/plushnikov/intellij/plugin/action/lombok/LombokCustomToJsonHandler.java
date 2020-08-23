package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.psi.*;
import lombok.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author lihongbin
 */
public class LombokCustomToJsonHandler extends BaseLombokHandler {


  protected void processClass(@NotNull PsiClass psiClass) {
    delToJsonMethod(psiClass);
    deLFormJsonMethod(psiClass);
    addAnnotation(psiClass, JsonSerializable.class);
  }

  private void deLFormJsonMethod(@NotNull PsiClass psiClass) {
    for (PsiMethod method : getFromJsonMethod(psiClass)) {
      method.delete();
    }
  }

  private PsiMethod[] getFromJsonMethod(PsiClass psiClass) {
    PsiMethod[] methods = psiClass.findMethodsByName("fromJson", false);
    return (PsiMethod[]) Arrays.stream(methods)
      .filter(x -> x.getParameters().length == 0).toArray();
  }

  private void delToJsonMethod(@NotNull PsiClass psiClass) {
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiClassType stringClassType = factory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, psiClass.getResolveScope());
    PsiMethod toJsonMethod = findPublicNonStaticMethod(psiClass, "toJson", stringClassType, PsiType.NULL);
    if (null != toJsonMethod) {
      toJsonMethod.delete();
    }
  }

}
