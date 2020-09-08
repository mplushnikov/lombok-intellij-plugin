package de.plushnikov.intellij.plugin.processor;

import com.intellij.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.JavaVarTypeUtil;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.settings.ProjectSettings;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class ValProcessor extends AbstractProcessor {

  private static final String LOMBOK_VAL_NAME = "val";
  private static final String LOMBOK_VAR_NAME = "var";
  private static final String LOMBOK_VAL_FQN = "lombok.val";
  private static final String LOMBOK_VAR_FQN = "lombok.var";
  private static final String LOMBOK_VAR_EXPERIMENTAL_FQN = "lombok.experimental.var";

  @SuppressWarnings("deprecation")
  public ValProcessor() {
    super(PsiElement.class, val.class, lombok.experimental.var.class, lombok.var.class);
  }

  public static boolean isVal(@NotNull PsiVariable psiVariable) {
    if (psiVariable instanceof PsiLocalVariable) {
      return isVal((PsiLocalVariable) psiVariable);
    }
    if (!(psiVariable instanceof PsiParameter)) {
      return false;
    }
    PsiParameter psiParameter = (PsiParameter) psiVariable;
    PsiTypeElement typeElement = psiParameter.getTypeElement();
    if (typeElement == null) {
      return false;
    }
    return isPossibleVal(typeElement.getText()) && isVal(resolveQualifiedName(typeElement));
  }

  public static boolean isVar(@NotNull PsiVariable psiVariable) {
    if (psiVariable instanceof PsiLocalVariable) {
      return isVar((PsiLocalVariable) psiVariable);
    }
    if (!(psiVariable instanceof PsiParameter)) {
      return false;
    }
    PsiParameter psiParameter = (PsiParameter) psiVariable;
    PsiTypeElement typeElement = psiParameter.getTypeElement();
    if (typeElement == null) {
      return false;
    }
    return isPossibleVar(typeElement.getText()) && isVar(resolveQualifiedName(typeElement));
  }

  public static boolean isVal(@NotNull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.getInitializer() != null) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleVal(typeElement.getText()) && isVal(resolveQualifiedName(typeElement));
    }
    return false;
  }

  public static boolean isVar(@NotNull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.getInitializer() != null) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleVar(typeElement.getText()) && isVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private static boolean isValOrVar(@NotNull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.getInitializer() != null) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleValOrVar(typeElement.getText()) && isValOrVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private boolean isValOrVarForEach(@NotNull PsiParameter psiParameter) {
    if (psiParameter.getParent() instanceof PsiForeachStatement) {
      final PsiTypeElement typeElement = psiParameter.getTypeElement();
      return null != typeElement && isPossibleValOrVar(typeElement.getText()) && isValOrVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private static boolean isValOrVar(@Nullable String fullQualifiedName) {
    return isVal(fullQualifiedName) || isVar(fullQualifiedName);
  }

  private static boolean isPossibleValOrVar(@Nullable String shortName) {
    return isPossibleVal(shortName) || isPossibleVar(shortName);
  }

  private static boolean isPossibleVal(@Nullable String shortName) {
    return LOMBOK_VAL_NAME.equals(shortName);
  }

  private static boolean isVal(@Nullable String fullQualifiedName) {
    return LOMBOK_VAL_FQN.equals(fullQualifiedName);
  }

  private static boolean isPossibleVar(@Nullable String shortName) {
    return LOMBOK_VAR_NAME.equals(shortName);
  }

  private static boolean isVar(@Nullable String fullQualifiedName) {
    return LOMBOK_VAR_FQN.equals(fullQualifiedName) || LOMBOK_VAR_EXPERIMENTAL_FQN.equals(fullQualifiedName);
  }

  @Nullable
  private static String resolveQualifiedName(@NotNull PsiTypeElement typeElement) {
    PsiJavaCodeReferenceElement reference = typeElement.getInnermostComponentReferenceElement();
    if (reference == null) {
      return null;
    }

    return reference.getQualifiedName();
  }

  @Override
  public boolean isEnabled(@NotNull Project project) {
    return ProjectSettings.isEnabled(project, ProjectSettings.IS_VAL_ENABLED);
  }

  @NotNull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@NotNull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation) {
    return Collections.emptyList();
  }

  public void verifyVariable(@NotNull final PsiLocalVariable psiLocalVariable, @NotNull final ProblemsHolder holder) {
    final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
    final String typeElementText = typeElement.getText();
    boolean isVal = isPossibleVal(typeElementText) && isVal(resolveQualifiedName(typeElement));
    boolean isVar = isPossibleVar(typeElementText) && isVar(resolveQualifiedName(typeElement));
    final String ann = isVal ? "val" : "var";
    if (isVal || isVar) {
      final PsiExpression initializer = psiLocalVariable.getInitializer();
      if (initializer == null) {
        holder.registerProblem(psiLocalVariable, "'" + ann + "' on a local variable requires an initializer expression", ProblemHighlightType.ERROR);
      } else if (initializer instanceof PsiArrayInitializerExpression) {
        holder.registerProblem(psiLocalVariable, "'" + ann + "' is not compatible with array initializer expressions. Use the full form (new int[] { ... } instead of just { ... })", ProblemHighlightType.ERROR);
      } else if (initializer instanceof PsiLambdaExpression) {
        holder.registerProblem(psiLocalVariable, "'" + ann + "' is not allowed with lambda expressions.", ProblemHighlightType.ERROR);
      } else if (isVal) {
        final PsiElement typeParentParent = psiLocalVariable.getParent();
        if (typeParentParent instanceof PsiDeclarationStatement && typeParentParent.getParent() instanceof PsiForStatement) {
          holder.registerProblem(psiLocalVariable, "'" + ann + "' is not allowed in old-style for loops", ProblemHighlightType.ERROR);
        }
      }
    }
  }

  public void verifyParameter(@NotNull final PsiParameter psiParameter, @NotNull final ProblemsHolder holder) {
    final PsiTypeElement typeElement = psiParameter.getTypeElement();
    final String typeElementText = null != typeElement ? typeElement.getText() : null;
    boolean isVal = isPossibleVal(typeElementText) && isVal(resolveQualifiedName(typeElement));
    boolean isVar = isPossibleVar(typeElementText) && isVar(resolveQualifiedName(typeElement));
    if (isVar || isVal) {
      PsiElement scope = psiParameter.getDeclarationScope();
      boolean isForeachStatement = scope instanceof PsiForeachStatement;
      boolean isForStatement = scope instanceof PsiForStatement;
      if (isVal && !isForeachStatement) {
        holder.registerProblem(psiParameter, "'val' works only on local variables and on foreach loops", ProblemHighlightType.ERROR);
      } else if (isVar && !(isForeachStatement || isForStatement)) {
        holder.registerProblem(psiParameter, "'var' works only on local variables and on for/foreach loops", ProblemHighlightType.ERROR);
      }
    }
  }

  @Nullable
  public PsiType inferType(PsiTypeElement typeElement) {
    PsiType psiType = null;

    final PsiElement parent = typeElement.getParent();
    if ((parent instanceof PsiLocalVariable && isValOrVar((PsiLocalVariable) parent)) ||
      (parent instanceof PsiParameter && isValOrVarForEach((PsiParameter) parent))) {

      if (parent instanceof PsiLocalVariable) {
        psiType = processLocalVariableInitializer(((PsiLocalVariable) parent).getInitializer());
      } else {
        psiType = processParameterDeclaration(((PsiParameter) parent).getDeclarationScope());
      }

      if (null == psiType) {
        psiType = PsiType.getJavaLangObject(typeElement.getManager(), typeElement.getResolveScope());
      }
    }
    return psiType;
  }

  private static final RecursionGuard<PsiExpression> guard = RecursionManager.createGuard("lombokValGuard");

  private PsiType processLocalVariableInitializer(final PsiExpression psiExpression) {
    PsiType result = null;
    if (null != psiExpression && !(psiExpression instanceof PsiArrayInitializerExpression)) {

// WORKAROUND FROM https://github.com/mplushnikov/lombok-intellij-plugin/pull/804/files
//      if (psiExpression instanceof PsiMethodCallExpression) {
//        forceGenericLambdaArgTypePreComputation((PsiMethodCallExpression) psiExpression);
//      }

//    Peter Gromov Idee
//      if (guard.currentStack().contains(psiExpression))
//        return PsiType.NULL;

      result = guard.doPreventingRecursion(psiExpression, true, () -> {
        PsiType type = psiExpression.getType();
        // This is how IntelliJ resolves intersection types.
        // This way auto-completion won't show unavailable methods.
        if (type instanceof PsiIntersectionType) {
          PsiType[] conjuncts = ((PsiIntersectionType) type).getConjuncts();
          if (conjuncts.length > 0) {
            return conjuncts[0];
          }
        }
        if (type != null) {
          //Get upward projection so you don't get types with missing diamonds.
          return JavaVarTypeUtil.getUpwardProjection(type);
        }
        return null;
      });
    }

    return result;
  }

  /**
   * When the val/var is assigned to the result of a method call
   * parameterized in the return type of a lambda (e.g. Optional.map),
   * we must first force pre-computation of the lambda return type, or
   * it will always return e.g. Optional<Object> instead of the target
   * type. See issue https://github.com/mplushnikov/lombok-intellij-plugin/issues/802
   * <p>
   * Current theory for why this works is that it forces either population
   * or use of com.intellij.psi.ThreadLocalTypes.myMap with the appropriate
   * type due to substitution in com.intellij.psi.LambdaUtil, which makes followup
   * calls to getType succeed.
   *
   * @param methodCallExpr e.g. "stringOpt.map(str -> Integer.valueOf(str))"
   */
  private void forceGenericLambdaArgTypePreComputation(PsiMethodCallExpression methodCallExpr) {
    PsiExpressionList methodArgs = methodCallExpr.getArgumentList(); // e.g. (str -> Integer.valueOf(str))
    for (PsiExpression methodArg : methodArgs.getExpressions()) {
      if (methodArg instanceof PsiLambdaExpression) {
        PsiLambdaExpression lambdaExpr = (PsiLambdaExpression) methodArg; // e.g. str -> Integer.valueOf(str)
        LambdaUtil.getFunctionalInterfaceType(lambdaExpr, true); // e.g. Function1<String, Integer>
      }
    }
  }

  private PsiType processParameterDeclaration(PsiElement parentDeclarationScope) {
    PsiType result = null;
    if (parentDeclarationScope instanceof PsiForeachStatement) {
      final PsiForeachStatement foreachStatement = (PsiForeachStatement) parentDeclarationScope;
      final PsiExpression iteratedValue = foreachStatement.getIteratedValue();
      if (iteratedValue != null) {
        result = JavaGenericsUtil.getCollectionItemType(iteratedValue);
      }
    }
    return result;
  }
}
