package de.plushnikov.intellij.plugin.action.lombok;

import com.hundsun.jres.studio.annotation.JRESEqualsAndHashCode;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

public class LombokJresEqualsAndHashcodeHandler extends BaseLombokHandler {

  protected void processClass(@NotNull PsiClass psiClass) {
    final PsiMethod equalsMethod = findPublicNonStaticMethod(psiClass, "equals", PsiType.BOOLEAN,
      PsiType.getJavaLangObject(psiClass.getManager(), psiClass.getResolveScope()));
    if (null != equalsMethod) {
      equalsMethod.delete();
    }

    final PsiMethod hashCodeMethod = findPublicNonStaticMethod(psiClass, "hashCode", PsiType.INT);
    if (null != hashCodeMethod) {
      hashCodeMethod.delete();
    }

    addAnnotation(psiClass, JRESEqualsAndHashCode.class);
  }
}
