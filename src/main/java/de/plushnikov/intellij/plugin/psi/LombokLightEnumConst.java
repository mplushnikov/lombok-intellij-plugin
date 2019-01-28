package de.plushnikov.intellij.plugin.psi;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import com.twelvemonkeys.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LombokLightEnumConst extends LightElement implements PsiEnumConstant {
  private PsiEnumConstant delegate;
  private PsiClass containingClass;

  public LombokLightEnumConst(@NotNull PsiManager manager, @NotNull PsiEnumConstant delegate, @NotNull PsiClass containingClass) {
    super(manager, JavaLanguage.INSTANCE);
    Validate.isTrue(containingClass.isEnum(), "must be of enum type");
    this.delegate = delegate;
    this.containingClass = containingClass;
  }

  @Override
  public PsiElement getParent() {
    return getContainingClass();
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return delegate.getIcon(flags);
  }

  @Override
  public String toString() {
    return "Light PSI enum const: " + getName();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void setInitializer(@Nullable PsiExpression initializer) throws IncorrectOperationException {
    delegate.setInitializer(initializer);
  }

  @Override
  public void normalizeDeclaration() throws IncorrectOperationException {
    delegate.normalizeDeclaration();
  }

  @Nullable
  @Override
  public Object computeConstantValue() {
    return this;
  }

  @NotNull
  @Override
  public PsiIdentifier getNameIdentifier() {
    return delegate.getNameIdentifier();
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    return delegate.setName(name);
  }

  @NotNull
  @Override
  public PsiType getType() {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(containingClass);
  }

  @Nullable
  @Override
  public PsiTypeElement getTypeElement() {
    return delegate.getTypeElement();
  }

  @Nullable
  @Override
  public PsiExpression getInitializer() {
    return delegate.getInitializer();
  }

  @Override
  public boolean hasInitializer() {
    return delegate.hasInitializer();
  }

  @Nullable
  @Override
  public PsiExpressionList getArgumentList() {
    return delegate.getArgumentList();
  }

  @Nullable
  @Override
  public PsiMethod resolveMethod() {
    return delegate.resolveMethod();
  }

  @NotNull
  @Override
  public JavaResolveResult resolveMethodGenerics() {
    return delegate.resolveMethodGenerics();
  }

  @Nullable
  @Override
  public PsiEnumConstantInitializer getInitializingClass() {
    return delegate.getInitializingClass();
  }

  @NotNull
  @Override
  public PsiEnumConstantInitializer getOrCreateInitializingClass() {
    return delegate.getOrCreateInitializingClass();
  }

  @Nullable
  @Override
  public PsiMethod resolveConstructor() {
    return delegate.resolveConstructor();
  }

  @Override
  public boolean isDeprecated() {
    return delegate.isDeprecated();
  }

  @Nullable
  @Override
  public PsiDocComment getDocComment() {
    return delegate.getDocComment();
  }

  @Nullable
  @Override
  public PsiClass getContainingClass() {
    return containingClass;
  }

  @Nullable
  @Override
  public PsiModifierList getModifierList() {
    return delegate.getModifierList();
  }

  @Override
  public boolean hasModifierProperty(@NotNull String name) {
    return delegate.hasModifierProperty(name);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor) visitor).visitEnumConstant(this);
    } else {
      visitor.visitElement(this);
    }
  }
}
