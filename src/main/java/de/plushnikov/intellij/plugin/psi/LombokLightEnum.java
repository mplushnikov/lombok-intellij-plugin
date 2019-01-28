package de.plushnikov.intellij.plugin.psi;

import com.intellij.lang.Language;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.light.AbstractLightClass;
import com.intellij.psi.impl.source.ClassInnerStuffCache;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiUtil;
import com.twelvemonkeys.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class LombokLightEnum extends AbstractLightClass implements PsiExtensibleClass {
  private final PsiClass delegate;
  private final PsiElement navigationElement;
  private final PsiClass containingClass;
  private final List<PsiField> myFields;
  private final ClassInnerStuffCache classInnerStuffCache;
  private final String accessLevel;
  private final LombokLightModifierList modifierList;

  public LombokLightEnum(PsiManager manager, Language language, @NotNull PsiClass delegate, @NotNull String accessLevel, @NotNull PsiElement navigationElement, @Nullable PsiClass containingClass) {
    super(manager, language);
    Validate.isTrue(delegate.isEnum(), "must be of enum type");
    this.delegate = delegate;
    this.navigationElement = navigationElement;
    this.containingClass = containingClass;
    this.myFields = new ArrayList<>();
    this.classInnerStuffCache = new ClassInnerStuffCache(this);
    this.accessLevel = accessLevel;
    Set<String> implicitModifiers = new HashSet<>();
    if (containingClass != null) {
      implicitModifiers.add(PsiModifier.STATIC);
      implicitModifiers.add(PsiModifier.FINAL);
    }
    this.modifierList = new LombokLightModifierList(manager, language, implicitModifiers, accessLevel);
  }

  @Override
  public boolean isEnum() {
    return true;
  }

  @NotNull
  @Override
  public PsiField[] getFields() {
    return myFields.toArray(new PsiField[0]);
  }


  public void addEnumConstant(PsiEnumConstant enumConstant) {
    myFields.add(enumConstant);
  }

  @Override
  public String toString() {
    return "Light PSI enum: " + getName();
  }

  @Nullable
  @Override
  public String getName() {
    return delegate.getName();
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return delegate.getIcon(flags);
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    return navigationElement;
  }

  @Nullable
  @Override
  public PsiClass getContainingClass() {
    return containingClass;
  }

  @Nullable
  @Override
  public String getQualifiedName() {
    return containingClass.getName() + "." + this.delegate.getName();
  }

  @Override
  public PsiElement getParent() {
    return getContainingClass();
  }

  @Override
  public PsiElement getContext() {
    return getContainingClass();
  }

  @NotNull
  @Override
  public PsiClass getDelegate() {
    return delegate;
  }

  @NotNull
  @Override
  public PsiElement copy() {
    return new LombokLightEnum(getManager(), getLanguage(), delegate, accessLevel, navigationElement, containingClass);
  }

  @Nullable
  @Override
  public PsiModifierList getModifierList() {
    return modifierList;
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
    if (isEnum()) {
      if (!PsiClassImplUtil.processDeclarationsInEnum(processor, state, classInnerStuffCache)) return false;
    }

    LanguageLevel level = PsiUtil.getLanguageLevel(place);
    return PsiClassImplUtil.processDeclarationsInClass(this, processor, state, null, lastParent, place, level, false);
  }

  @NotNull
  @Override
  public List<PsiField> getOwnFields() {
    return Arrays.asList(getFields());
  }

  @NotNull
  @Override
  public List<PsiMethod> getOwnMethods() {
    return Arrays.asList(getMethods());
  }

  @NotNull
  @Override
  public List<PsiClass> getOwnInnerClasses() {
    return Arrays.asList(getInnerClasses());
  }

}
