package de.plushnikov.intellij.plugin.handler;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.Builder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;


public class BuilderHandler {

  public static boolean isDefaultBuilderValue(HighlightInfo highlightInfo, PsiFile file) {
    PsiField field = PsiTreeUtil.getParentOfType(file.findElementAt(highlightInfo.getStartOffset()), PsiField.class);
    if (field == null) {
      return false;
    }

    return PsiAnnotationSearchUtil.isAnnotatedWith(field, Builder.Default.class.getCanonicalName());
  }
}
