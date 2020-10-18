package de.plushnikov.intellij.plugin.intention.valvar.to;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.PsiDiamondTypeUtil;
import de.plushnikov.intellij.plugin.intention.valvar.AbstractValVarIntentionAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractReplaceExplicitTypeWithVariableIntentionAction extends AbstractValVarIntentionAction {

  private final String variableClassName;

  public AbstractReplaceExplicitTypeWithVariableIntentionAction(String variableClassName) {
    this.variableClassName = variableClassName;
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return "Replace explicit type with '" + StringUtil.getShortName(variableClassName) + "' (Lombok)";
  }

  @Override
  public boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement context) {
    PsiElement[] declaredElements = context.getDeclaredElements();
    if (declaredElements.length != 1) {
      return false;
    }
    PsiElement declaredElement = declaredElements[0];
    if (!(declaredElement instanceof PsiLocalVariable)) {
      return false;
    }
    PsiLocalVariable localVariable = (PsiLocalVariable) declaredElement;
    if (!localVariable.hasInitializer()) {
      return false;
    }
    PsiExpression initializer = localVariable.getInitializer();
    if (initializer instanceof PsiArrayInitializerExpression || initializer instanceof PsiLambdaExpression) {
      return false;
    }
    if (localVariable.getTypeElement().isInferredType()) {
      return false;
    }
    return isAvailableOnDeclarationCustom(context, localVariable);
  }

  protected abstract boolean isAvailableOnDeclarationCustom(PsiDeclarationStatement context, PsiLocalVariable localVariable);

  @Override
  public void invokeOnDeclarationStatement(PsiDeclarationStatement declarationStatement) {
    if (declarationStatement.getDeclaredElements().length == 1) {
      PsiLocalVariable localVariable = (PsiLocalVariable) declarationStatement.getDeclaredElements()[0];
      invokeOnVariable(localVariable);
    }
  }

  @Override
  public void invokeOnVariable(PsiVariable psiVariable) {
    Project project = psiVariable.getProject();
    psiVariable.normalizeDeclaration();
    PsiTypeElement typeElement = psiVariable.getTypeElement();
    if (typeElement == null || typeElement.isInferredType()) {
      return;
    }

    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
    PsiClass variablePsiClass = JavaPsiFacade.getInstance(project).findClass(variableClassName, psiVariable.getResolveScope());
    if (variablePsiClass == null) {
      return;
    }
    PsiJavaCodeReferenceElement referenceElementByFQClassName = elementFactory.createReferenceElementByFQClassName(variableClass.getName(), psiVariable.getResolveScope());
    typeElement = (PsiTypeElement) expandDiamondsAndReplaceExplicitTypeWithVar(typeElement, typeElement);
    typeElement.deleteChildRange(typeElement.getFirstChild(), typeElement.getLastChild());
    typeElement.add(referenceElementByFQClassName);
    removeRedundantTypeArguments(psiVariable);
    executeAfterReplacing(psiVariable);
    CodeStyleManager.getInstance(project).reformat(psiVariable);
  }

  protected abstract void executeAfterReplacing(PsiVariable psiVariable);

  /**
   * Ensure that diamond inside initializer is expanded, then replace variable type with var
   * From IntroduceVariableBase
   */
  private PsiElement expandDiamondsAndReplaceExplicitTypeWithVar(PsiTypeElement typeElement, PsiElement context) {
    PsiElement parent = typeElement.getParent();
    if (parent instanceof PsiVariable) {
      PsiExpression copyVariableInitializer = ((PsiVariable) parent).getInitializer();
      if (copyVariableInitializer instanceof PsiNewExpression) {
        final PsiDiamondType.DiamondInferenceResult diamondResolveResult =
          PsiDiamondTypeImpl.resolveInferredTypesNoCheck((PsiNewExpression) copyVariableInitializer, copyVariableInitializer);
        if (!diamondResolveResult.getInferredTypes().isEmpty()) {
          PsiDiamondTypeUtil.expandTopLevelDiamondsInside(copyVariableInitializer);
        }
      }
    }

    return typeElement.replace(JavaPsiFacade.getElementFactory(context.getProject()).createTypeElementFromText("var", context));
  }
}
