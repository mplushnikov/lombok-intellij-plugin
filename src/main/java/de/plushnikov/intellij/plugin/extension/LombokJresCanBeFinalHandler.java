package de.plushnikov.intellij.plugin.extension;

import com.hundsun.jres.studio.annotation.JRESData;
import com.hundsun.jres.studio.annotation.JRESSetter;
import com.intellij.codeInspection.canBeFinal.CanBeFinalHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import lombok.Value;

/**
 * Handler to produce a veto for elements with lombok methods behind
 */
public class LombokJresCanBeFinalHandler extends CanBeFinalHandler {
  @Override
  public boolean canBeFinal(PsiMember member) {
    if (member instanceof PsiField) {
      if (PsiAnnotationSearchUtil.isAnnotatedWith(member, JRESSetter.class)) {
        return false;
      }

      final PsiClass psiClass = PsiTreeUtil.getParentOfType(member, PsiClass.class);
      return null == psiClass || !PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, JRESSetter.class, JRESData.class, Value.class);
    }
    return true;
  }
}
