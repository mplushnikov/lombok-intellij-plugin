package de.plushnikov.intellij.plugin.intention.valvar;

import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiDiamondTypeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import de.plushnikov.intellij.plugin.intention.AbstractLombokIntentionAction;
import de.plushnikov.intellij.plugin.settings.ProjectSettings;
import de.plushnikov.intellij.plugin.util.CommentTracker;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractValVarIntentionAction extends AbstractLombokIntentionAction implements LowPriorityAction {

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    if (!super.isAvailable(project, editor, element)) {
      return false;
    }
    if (!ProjectSettings.isEnabled(project, ProjectSettings.IS_VAL_ENABLED)) {
      return false;
    }
    if (element instanceof PsiCompiledElement || !canModifyBP(element) || !element.getLanguage().is(JavaLanguage.INSTANCE)) {
      return false;
    }

    setText(getFamilyName());

    PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class, false, PsiClass.class, PsiCodeBlock.class);
    if (parameter != null) {
      return isAvailableOnVariable(parameter);
    }
    PsiDeclarationStatement context = PsiTreeUtil.getParentOfType(element, PsiDeclarationStatement.class, false, PsiClass.class, PsiCodeBlock.class);
    return context != null && isAvailableOnDeclarationStatement(context);
  }

  public static boolean canModifyBP(PsiElement element) {
    return element.getManager().isInProject(element) || ScratchFileService.isInScratchRoot(PsiUtilCore.getVirtualFile(element));
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    final PsiDeclarationStatement declarationStatement = PsiTreeUtil.getParentOfType(element, PsiDeclarationStatement.class);

    if (declarationStatement != null) {
      invokeOnDeclarationStatement(declarationStatement);
      return;
    }

    final PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
    if (parameter != null) {
      invokeOnVariable(parameter);
    }
  }

  public abstract boolean isAvailableOnVariable(PsiVariable psiVariable);

  public abstract boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement psiDeclarationStatement);

  public abstract void invokeOnVariable(PsiVariable psiVariable);

  public abstract void invokeOnDeclarationStatement(PsiDeclarationStatement psiDeclarationStatement);

  // from RemoveRedundantTypeArgumentsUtil
  private PsiElement replaceExplicitWithDiamond(PsiElement psiElement) {
    PsiElement replacement = createExplicitReplacementBP(psiElement);
    return replacement == null ? psiElement : psiElement.replace(replacement);
  }

  //from PsiDiamondTypeUtil.createExplicitReplacement
  private PsiElement createExplicitReplacementBP(PsiElement psiElement) {
    if (psiElement instanceof PsiReferenceParameterList) {
      final PsiNewExpression expression =
        (PsiNewExpression) JavaPsiFacade.getElementFactory(psiElement.getProject()).createExpressionFromText("new a<>()", psiElement);
      final PsiJavaCodeReferenceElement classReference = expression.getClassReference();
      final PsiReferenceParameterList parameterList = classReference.getParameterList();
      return parameterList;
    }
    return null;
  }

  /**
   * Removes redundant type arguments which appear in any descendants of the supplied element.
   *
   * @param element element to start the replacement from
   *                <p>
   *                from RemoveRedundantTypeArgumentsUtil
   */
  public void removeRedundantTypeArguments(PsiElement element) {
    for (PsiNewExpression newExpression : PsiTreeUtil.collectElementsOfType(element, PsiNewExpression.class)) {
      PsiJavaCodeReferenceElement classReference = newExpression.getClassOrAnonymousClassReference();
      if (classReference != null && PsiDiamondTypeUtil.canCollapseToDiamond(newExpression, newExpression, null)) {
        replaceExplicitWithDiamond(classReference.getParameterList());
      }
    }
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(element.getProject());
    for (PsiMethodCallExpression call : PsiTreeUtil.collectElementsOfType(element, PsiMethodCallExpression.class)) {
      PsiType[] arguments = call.getTypeArguments();
      if (arguments.length == 0) continue;
      PsiMethod method = call.resolveMethod();
      if (method != null) {
        PsiTypeParameter[] parameters = method.getTypeParameters();
        if (arguments.length == parameters.length && PsiDiamondTypeUtil.areTypeArgumentsRedundant(arguments, call, false, method, parameters)) {
          PsiMethodCallExpression expr = (PsiMethodCallExpression) factory.createExpressionFromText("foo()", null);
          new CommentTracker().replaceAndRestoreComments(call.getTypeArgumentList(), expr.getTypeArgumentList());
        }
      }
    }
  }
}
