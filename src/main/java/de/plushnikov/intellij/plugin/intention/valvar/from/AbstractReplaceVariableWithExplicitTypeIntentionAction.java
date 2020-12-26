package de.plushnikov.intellij.plugin.intention.valvar.from;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.intention.valvar.AbstractValVarIntentionAction;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractReplaceVariableWithExplicitTypeIntentionAction extends AbstractValVarIntentionAction {

  private final String variableClassName;

  public AbstractReplaceVariableWithExplicitTypeIntentionAction(String variableClassName) {
    this.variableClassName = variableClassName;
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return LombokBundle.message("replace.0.with.explicit.type.lombok", StringUtil.getShortName(variableClassName));
  }

  @Override
  public boolean isAvailableOnVariable(PsiVariable psiVariable) {
    if (LombokClassNames.VAL.equals(variableClassName)) {
      return ValProcessor.isVal(psiVariable);
    }
    if (LombokClassNames.VAR.equals(variableClassName)) {
      return ValProcessor.isVar(psiVariable);
    }
    return false;
  }

  @Override
  public boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement context) {
    if (context.getDeclaredElements().length <= 0) {
      return false;
    }
    PsiElement declaredElement = context.getDeclaredElements()[0];
    if (!(declaredElement instanceof PsiLocalVariable)) {
      return false;
    }
    return isAvailableOnVariable((PsiLocalVariable) declaredElement);
  }

  @Override
  public void invokeOnDeclarationStatement(PsiDeclarationStatement declarationStatement) {
    if (declarationStatement.getDeclaredElements().length > 0) {
      PsiElement declaredElement = declarationStatement.getDeclaredElements()[0];
      if (declaredElement instanceof PsiLocalVariable) {
        invokeOnVariable((PsiLocalVariable) declaredElement);
      }
    }
  }

  @Override
  public void invokeOnVariable(PsiVariable psiVariable) {
    PsiTypeElement psiTypeElement = psiVariable.getTypeElement();
    if (psiTypeElement == null) {
      return;
    }
    replaceWithExplicitTypeBP(psiTypeElement);
    removeRedundantTypeArguments(psiVariable);
    executeAfterReplacing(psiVariable);
    CodeStyleManager.getInstance(psiVariable.getProject()).reformat(psiVariable);
  }

  protected abstract void executeAfterReplacing(PsiVariable psiVariable);

  @Nullable
  // from PsiTypesUtil.replaceWithExplicitType
  private PsiTypeElement replaceWithExplicitTypeBP(PsiTypeElement typeElement) {
    PsiType type = typeElement.getType();
    if (!isDenotableType(type, typeElement)) {
      return null;
    }
    Project project = typeElement.getProject();
    PsiTypeElement typeElementByExplicitType = JavaPsiFacade.getElementFactory(project).createTypeElement(type);
    PsiElement explicitTypeElement = typeElement.replace(typeElementByExplicitType);
    explicitTypeElement = JavaCodeStyleManager.getInstance(project).shortenClassReferences(explicitTypeElement);
    return (PsiTypeElement) CodeStyleManager.getInstance(project).reformat(explicitTypeElement);
  }

  private boolean isDenotableType(@Nullable PsiType type, @NotNull PsiElement context) {
    if (type == null || type instanceof PsiWildcardType) return false;
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(context.getProject());
    try {
      PsiType typeAfterReplacement = elementFactory.createTypeElementFromText(type.getCanonicalText(), context).getType();
      return type.equals(typeAfterReplacement);
    } catch (IncorrectOperationException e) {
      return false;
    }
  }
}
