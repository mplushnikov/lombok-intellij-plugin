package de.plushnikov.intellij.plugin.intention;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
public class ReplaceWithLombokAnnotationAction extends AbstractLombokIntentionAction {

  public ReplaceWithLombokAnnotationAction() {
    super();
    setText("Replace with Lombok");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    PsiElement parent = PsiTreeUtil.getParentOfType(element, PsiVariable.class, PsiClass.class, PsiMethod.class);

    if (parent instanceof PsiField) {
      handleField((PsiField) parent);
    } else if (parent instanceof PsiMethod) {
      handleMethod((PsiMethod) parent);
    }
  }

  private void handleMethod(PsiMethod parent) {
    Optional.ofNullable(findAnchorFieldForGetter(parent))
      .map(PsiField::getModifierList)
      .ifPresent(modifierList -> replaceWithAnnotation(modifierList, parent, Getter.class));
    Optional.ofNullable(findAnchorFieldForSetter(parent))
      .map(PsiField::getModifierList)
      .ifPresent(modifierList -> replaceWithAnnotation(modifierList, parent, Setter.class));
  }

  private void handleField(PsiField field) {
    PsiModifierList modifierList = field.getModifierList();
    if (modifierList == null) {
      // it should never happen but just in case
      return;
    }

    Collection<PsiMethod> methods = Optional.ofNullable(field.getContainingClass())
      .map(PsiClassUtil::collectClassMethodsIntern)
      .orElse(Collections.emptyList());

    // replace getter if it matches the requirements
    String getterName = LombokUtils.getGetterName(field);
    methods.stream()
      .filter(method -> getterName.equals(method.getName()))
      .findAny()
      .ifPresent(method -> Optional.ofNullable(findAnchorFieldForGetter(method))
        .filter(anchorField -> anchorField == field)
        .ifPresent(f -> replaceWithAnnotation(modifierList, method, Getter.class))
      );

    // replace setter if it matches the requirements
    String setterName = LombokUtils.getSetterName(field);
    methods.stream()
      .filter(method -> setterName.equals(method.getName()))
      .findAny()
      .ifPresent(method -> Optional.ofNullable(findAnchorFieldForSetter(method))
        .filter(anchorField -> anchorField == field)
        .ifPresent(f -> replaceWithAnnotation(modifierList, method, Setter.class))
      );
  }

  @Nullable
  private PsiField findAnchorFieldForSetter(PsiMethod method) {
    // it seems wrong to replace abstract possible getters
    // abstract methods maybe the part of interface so let them live
    if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return null;
    }

    // check the parameter list
    // it should have 1 parameter with the same type
    if (Optional.of(method.getParameterList())
      .filter(paramList -> paramList.getParametersCount() == 1)
      .map(paramList -> paramList.getParameter(0))
      .map(PsiParameter::getType)
      .filter(expectedType -> Optional.ofNullable(method.getContainingClass())
        .map(PsiClassUtil::collectClassFieldsIntern)
        .orElse(Collections.emptyList())
        .stream()
        .filter(field -> method.getName().equals(LombokUtils.getSetterName(field)))
        .noneMatch(field -> expectedType.equals(field.getType()))
      ).isPresent()) {
      return null;
    }

    PsiCodeBlock body = method.getBody();
    if (body == null) {
      return Optional.ofNullable(method.getContainingClass())
        .map(PsiClassUtil::collectClassFieldsIntern)
        .orElse(Collections.emptyList())
        .stream()
        .filter(field -> method.getName().equals(LombokUtils.getSetterName(field)))
        .findAny()
        .orElse(null);
    } else if (body.getStatementCount() == 1) {
      // validate that the method body doesn't contain anything additional
      // and also contain proper assign statement
      return Optional.of(body.getStatements()[0])
        .filter(PsiExpressionStatement.class::isInstance)
        .map(PsiExpressionStatement.class::cast)
        .map(PsiExpressionStatement::getExpression)
        .filter(PsiAssignmentExpression.class::isInstance)
        .map(PsiAssignmentExpression.class::cast)
        .map(PsiAssignmentExpression::getLExpression)
        .filter(PsiReferenceExpression.class::isInstance)
        .map(PsiReferenceExpression.class::cast)
        .map(PsiReferenceExpression::resolve)
        .filter(PsiField.class::isInstance)
        .map(PsiField.class::cast)
        .orElse(null);
    }

    return null;
  }

  @Nullable
  private PsiField findAnchorFieldForGetter(PsiMethod method) {
    // it seems wrong to replace abstract possible getters
    // abstract methods maybe the part of interface so let them live
    if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return null;
    }

    PsiCodeBlock body = method.getBody();
    if (body == null) {
      return Optional.ofNullable(method.getContainingClass())
        .map(PsiClassUtil::collectClassFieldsIntern)
        .orElse(Collections.emptyList())
        .stream()
        .filter(field -> method.getName().equals(LombokUtils.getGetterName(field)))
        .findAny()
        .orElse(null);
    } else if (body.getStatementCount() == 1) {
      return Optional.of(body.getStatements()[0])
        .filter(PsiReturnStatement.class::isInstance)
        .map(PsiReturnStatement.class::cast)
        .map(PsiReturnStatement::getReturnValue)
        .map(PsiUtil::deparenthesizeExpression)
        .filter(PsiReferenceExpression.class::isInstance)
        .map(PsiReferenceExpression.class::cast)
        .map(PsiReferenceExpression::resolve)
        .filter(PsiField.class::isInstance)
        .map(PsiField.class::cast)
        .orElse(null);
    }

    return null;
  }

  private void replaceWithAnnotation(PsiModifierList modifierList, PsiMethod method, Class<? extends Annotation> annotation) {
    method.delete();
    modifierList.addAnnotation(annotation.getCanonicalName());
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return "Replace with annotations (Lombok)";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    boolean parentAvailable = super.isAvailable(project, editor, element);
    return parentAvailable
      && element instanceof PsiIdentifier
      && PsiTreeUtil.getParentOfType(element, PsiField.class, PsiMethod.class) != null;
  }
}
