package de.plushnikov.intellij.plugin.util;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Plushnikov Michail
 */
public class PsiClassUtil {

  private static final Function<PsiElement, PsiMethod> PSI_ELEMENT_TO_METHOD_FUNCTION = new Function<PsiElement, PsiMethod>() {
    @Override
    public PsiMethod apply(PsiElement psiElement) {
      return (PsiMethod) psiElement;
    }
  };
  private static final Function<PsiElement, PsiField> PSI_ELEMENT_TO_FIELD_FUNCTION = new Function<PsiElement, PsiField>() {
    @Override
    public PsiField apply(PsiElement psiElement) {
      return (PsiField) psiElement;
    }
  };

  /**
   * Workaround to get all of original Methods of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to collect all of methods from
   * @return all intern methods of the class
   */
  @NotNull
  public static Collection<PsiMethod> collectClassMethodsIntern(@NotNull PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass) psiClass).getOwnMethods();
    } else {
      return Collections2.transform(
          Collections2.filter(Lists.newArrayList(psiClass.getChildren()), Predicates.instanceOf(PsiMethod.class)),
          PSI_ELEMENT_TO_METHOD_FUNCTION);
    }
  }

  /**
   * Workaround to get all of original Fields of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to collect all of fields from
   * @return all intern fields of the class
   */
  @NotNull
  public static Collection<PsiField> collectClassFieldsIntern(@NotNull PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass) psiClass).getOwnFields();
    } else {
      return Collections2.transform(
          Collections2.filter(Lists.newArrayList(psiClass.getChildren()), Predicates.instanceOf(PsiField.class)),
          PSI_ELEMENT_TO_FIELD_FUNCTION);
    }
  }

  @NotNull
  public static Collection<PsiMethod> collectClassConstructorIntern(@NotNull PsiClass psiClass) {
    final Collection<PsiMethod> psiMethods = collectClassMethodsIntern(psiClass);

    Collection<PsiMethod> classConstructors = new ArrayList<PsiMethod>(3);
    for (PsiMethod psiMethod : psiMethods) {
      if (psiMethod.isConstructor()) {
        classConstructors.add(psiMethod);
      }
    }
    return classConstructors;
  }

  @NotNull
  public static Collection<PsiMethod> collectClassStaticMethodsIntern(@NotNull PsiClass psiClass) {
    final Collection<PsiMethod> psiMethods = collectClassMethodsIntern(psiClass);

    Collection<PsiMethod> staticMethods = new ArrayList<PsiMethod>(5);
    for (PsiMethod psiMethod : psiMethods) {
      if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
        staticMethods.add(psiMethod);
      }
    }
    return staticMethods;
  }

  public static boolean hasSuperClass(@NotNull final PsiClass psiClass) {
    final PsiClass superClass = psiClass.getSuperClass();
    return (null != superClass && !CommonClassNames.JAVA_LANG_OBJECT.equals(superClass.getQualifiedName())
        || !superTypesIsEmptyOrObjectOnly(psiClass));
  }

  private static boolean superTypesIsEmptyOrObjectOnly(@NotNull final PsiClass psiClass) {
    // It returns abstract classes, but also Object.
    final PsiClassType[] superTypes = psiClass.getSuperTypes();
    return superTypes.length == 0 || superTypes.length > 1 || CommonClassNames.JAVA_LANG_OBJECT.equals(superTypes[0].getCanonicalText());
  }

  public static boolean hasMultiArgumentConstructor(@NotNull final PsiClass psiClass) {
    boolean result = false;
    final Collection<PsiMethod> definedConstructors = collectClassConstructorIntern(psiClass);
    for (PsiMethod psiMethod : definedConstructors) {
      if (psiMethod.getParameterList().getParametersCount() > 0) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Creates a PsiType for a PsiClass enriched with generic substitution information if available
   */
  @NotNull
  public static PsiType getTypeWithGenerics(@NotNull PsiClass psiClass) {
    PsiType result;
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiTypeParameter[] classTypeParameters = psiClass.getTypeParameters();
    if (classTypeParameters.length > 0) {
      Map<PsiTypeParameter, PsiType> substitutionMap = new HashMap<PsiTypeParameter, PsiType>();
      for (PsiTypeParameter typeParameter : classTypeParameters) {
        substitutionMap.put(typeParameter, factory.createType(typeParameter));
      }
      result = factory.createType(psiClass, factory.createSubstitutor(substitutionMap));
    } else {
      result = factory.createType(psiClass);
    }
    return result;
  }
  
  /**
   * Return true if class is final.
   *
   * @param psiClass
   * @return
   */
  public static boolean isFinalClass(@NotNull PsiClass psiClass) {
    boolean result = false;
    final PsiModifierList modifierList = psiClass.getModifierList();
    if (null != modifierList) {
      result = modifierList.hasModifierProperty(PsiModifier.FINAL);
    }
    return result;
  }
}
