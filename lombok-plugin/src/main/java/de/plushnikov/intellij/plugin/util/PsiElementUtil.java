package de.plushnikov.intellij.plugin.util;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Copy from com.siyeh.ig.psiutils
 * Classes: PsiElementUtil and EquivalenceChecker
 */
public class PsiElementUtil {
  /**
   * @param method              the method to compare to.
   * @param containingClassName the name of the class which contiains the
   *                            method.
   * @param returnType          the return type, specify null if any type matches
   * @param methodName          the name the method should have
   * @param parameterTypes      the type of the parameters of the method, specify
   *                            null if any number and type of parameters match or an empty array
   *                            to match zero parameters.
   * @return true, if the specified method matches the specified constraints,
   *         false otherwise
   */
  public static boolean methodMatches(
      @NotNull PsiMethod method,
      @NonNls @Nullable String containingClassName,
      @Nullable PsiType returnType,
      @NonNls @Nullable String methodName,
      @Nullable List<PsiType> parameterTypes) {
    final String name = method.getName();
    if (methodName != null && !methodName.equals(name)) {
      return false;
    }
    if (parameterTypes != null) {
      final PsiParameterList parameterList = method.getParameterList();
      if (parameterList.getParametersCount() != parameterTypes.size()) {
        return false;
      }
      final PsiParameter[] parameters = parameterList.getParameters();
      for (int i = 0; i < parameters.length; i++) {
        final PsiParameter parameter = parameters[i];
        final PsiType type = parameter.getType();
        final PsiType parameterType = parameterTypes.get(i);
        if (PsiType.NULL.equals(parameterType)) {
          continue;
        }
        if (parameterType != null &&
            !typesAreEquivalent(type,
                parameterType)) {
          return false;
        }
      }
    }
    if (returnType != null) {
      final PsiType methodReturnType = method.getReturnType();
      if (!typesAreEquivalent(returnType,
          methodReturnType)) {
        return false;
      }
    }
    if (containingClassName != null) {
      final PsiClass containingClass = method.getContainingClass();
      return InheritanceUtil.isInheritor(containingClass,
          containingClassName);
    }
    return true;
  }

  /**
   * @param method        the method to compare to.
   * @param returnType    the return type, specify null if any type matches
   * @param methodName    the name the method should have
   * @param parameterList the list of the parameters of the method, specify
   *                      null if any number and type of parameters match
   * @return true, if the specified method matches the specified constraints,
   *         false otherwise
   */
  public static boolean methodMatches(
      @NotNull PsiMethod method,
      @Nullable PsiType returnType,
      @NonNls @Nullable String methodName,
      @Nullable PsiParameterList parameterList) {
    final String name = method.getName();
    if (methodName != null && !methodName.equals(name)) {
      return false;
    }
    if (parameterList != null) {
      final PsiParameterList methodParameterList = method.getParameterList();
      if (methodParameterList.getParametersCount() != parameterList.getParametersCount()) {
        return false;
      }
      final PsiParameter[] methodParameters = methodParameterList.getParameters();
      final PsiParameter[] otherParameters = parameterList.getParameters();
      for (int i = 0; i < methodParameters.length; i++) {
        final PsiType type = methodParameters[i].getType();
        final PsiType parameterType = otherParameters[i].getType();
        if (PsiType.NULL.equals(parameterType)) {
          continue;
        }
        if (!typesAreEquivalent(type,
            parameterType)) {
          return false;
        }
      }
    }
    if (returnType != null) {
      final PsiType methodReturnType = method.getReturnType();
      if (!typesAreEquivalent(returnType,
          methodReturnType)) {
        return false;
      }
    }
    return true;
  }

  public static boolean typesAreEquivalent(
      @Nullable PsiType type1, @Nullable PsiType type2) {
    if (type1 == null) {
      return type2 == null;
    }
    if (type2 == null) {
      return false;
    }
    final String type1Text = type1.getCanonicalText();
    final String type2Text = type2.getCanonicalText();
    return type1Text.equals(type2Text);
  }

  public static boolean methodMatches(@NotNull Pair<PsiMethod, PsiSubstitutor> firstPair, @NotNull Pair<PsiMethod, PsiSubstitutor> secondPair) {
    final PsiMethod firstMethod = firstPair.getFirst();
    final PsiMethod secondMethod = secondPair.getFirst();
    if (!firstMethod.getName().equals(secondMethod.getName())) {
      return false;
    }

    PsiParameterList firstMethodParameterList = firstMethod.getParameterList();
    PsiParameterList secondMethodParameterList = secondMethod.getParameterList();
    if (firstMethodParameterList.getParametersCount() != secondMethodParameterList.getParametersCount()) {
      return false;
    }

    PsiParameter[] firstMethodParameterListParameters = firstMethodParameterList.getParameters();
    PsiParameter[] secondMethodParameterListParameters = secondMethodParameterList.getParameters();
    PsiSubstitutor firstSubstitutor = firstPair.getSecond();
    PsiSubstitutor secondSubstitutor = secondPair.getSecond();
    for (int i = 0; i < firstMethodParameterListParameters.length; i++) {
      PsiType firstMethodParameterListParameterType = firstSubstitutor.substitute(firstMethodParameterListParameters[i].getType());
      PsiType secondMethodParameterListParameterType = secondSubstitutor.substitute(secondMethodParameterListParameters[i].getType());
      if (PsiType.NULL.equals(firstMethodParameterListParameterType)) {
        continue;
      }
      if (!typesAreEquivalent(firstMethodParameterListParameterType, secondMethodParameterListParameterType)) {
        return false;
      }
    }

    return true;
  }
}
