package de.plushnikov.intellij.plugin.handler;

import com.intellij.codeInsight.CustomExceptionHandler;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SneakyThrowsExceptionHandler extends CustomExceptionHandler {

  private static final String JAVA_LANG_THROWABLE = "java.lang.Throwable";

  @Override
  public boolean isHandled(@Nullable PsiElement element, @NotNull PsiClassType exceptionType, PsiElement topElement) {
    // that exception may be already handled by regular try-catch statement
    if (topElement instanceof PsiTryStatement) {
      List<PsiType> caughtExceptions = Stream.of(((PsiTryStatement) topElement).getCatchBlockParameters())
        .map(PsiParameter::getType)
        .collect(Collectors.toList());
      if (isExceptionHandled(exceptionType, caughtExceptions)) {
        return false;
      }
    }

    if (!(topElement instanceof PsiCodeBlock)) {
      final PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      return psiMethod != null && isExceptionHandled(psiMethod, exceptionType);
    }
    return false;
  }

  private boolean isExceptionHandled(@NotNull PsiModifierListOwner psiModifierListOwner, PsiClassType exceptionClassType) {
    final PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiModifierListOwner, SneakyThrows.class);
    if (psiAnnotation == null) {
      return false;
    }

    final Collection<PsiType> sneakedExceptionTypes = PsiAnnotationUtil.getAnnotationValues(psiAnnotation, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME, PsiType.class);
    //Default SneakyThrows handles all exceptions
    return sneakedExceptionTypes.isEmpty()
      || sneakedExceptionTypes.iterator().next().equalsToText(JAVA_LANG_THROWABLE)
      || isExceptionHandled(exceptionClassType, sneakedExceptionTypes);
  }

  private boolean isExceptionHandled(@NotNull PsiClassType exceptionClassType, @NotNull Collection<PsiType> sneakedExceptionTypes) {
    for (PsiType sneakedExceptionType : sneakedExceptionTypes) {
      if (sneakedExceptionType.equalsToText(JAVA_LANG_THROWABLE) || sneakedExceptionType.equals(exceptionClassType)) {
        return true;
      }
    }

    final PsiClass unhandledExceptionClass = exceptionClassType.resolve();

    if (null != unhandledExceptionClass) {
      for (PsiType sneakedExceptionType : sneakedExceptionTypes) {
        if (sneakedExceptionType instanceof PsiClassType) {
          final PsiClass sneakedExceptionClass = ((PsiClassType) sneakedExceptionType).resolve();

          if (null != sneakedExceptionClass && unhandledExceptionClass.isInheritor(sneakedExceptionClass, true)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
