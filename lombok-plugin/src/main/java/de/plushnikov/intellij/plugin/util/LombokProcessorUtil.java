package de.plushnikov.intellij.plugin.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Plushnikov Michail
 */
public class LombokProcessorUtil {

  @Nullable
  public static String getMethodModifier(@NotNull PsiAnnotation psiAnnotation) {
    return convertAccessLevelToJavaModifier(getAnnotationValue(psiAnnotation, "value"));
  }

  @Nullable
  public static String getAccessVisibity(@NotNull PsiAnnotation psiAnnotation) {
    return convertAccessLevelToJavaString(getAnnotationValue(psiAnnotation, "access"));
  }

  private static String getAnnotationValue(final PsiAnnotation psiAnnotation, final String parameterName) {
    return PsiAnnotationUtil.getAnnotationValue(psiAnnotation, parameterName, String.class);
  }

  @Nullable
  public static String convertAccessLevelToJavaString(String value) {
    if (null == value || value.isEmpty() || value.equals("PUBLIC")) {
      return PsiKeyword.PUBLIC;
    }
    if (value.equals("MODULE")) {
      return "";
    }
    if (value.equals("PROTECTED")) {
      return PsiKeyword.PROTECTED;
    }
    if (value.equals("PACKAGE")) {
      return "";
    }
    if (value.equals("PRIVATE")) {
      return PsiKeyword.PRIVATE;
    }
    if (value.equals("NONE")) {
      return null;
    } else {
      return null;
    }
  }

  @Nullable
  private static String convertAccessLevelToJavaModifier(String value) {
    if (null == value || value.isEmpty() || value.equals("PUBLIC")) {
      return PsiModifier.PUBLIC;
    }
    if (value.equals("MODULE")) {
      return PsiModifier.PACKAGE_LOCAL;
    }
    if (value.equals("PROTECTED")) {
      return PsiModifier.PROTECTED;
    }
    if (value.equals("PACKAGE")) {
      return PsiModifier.PACKAGE_LOCAL;
    }
    if (value.equals("PRIVATE")) {
      return PsiModifier.PRIVATE;
    }
    if (value.equals("NONE")) {
      return null;
    } else {
      return null;
    }
  }
}
