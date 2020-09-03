package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.Convertable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Inspect and validate @JsonSerializable lombok annotation on a class
 * Creates toBean() and fromBean() method for fields of this class
 *
 * @author lihongbin
 */
public class CustomFormatBeanProcessor extends AbstractClassProcessor {

  private static final String TO_BEAN_FIELD_NAME = "toBean",
    FROM_BEAN_FIELD_NAME = "fromBean";

  public CustomFormatBeanProcessor() {
    super(PsiMethod.class, Convertable.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final boolean result = validateAnnotationOnRigthType(psiClass, builder);
    if (result) {
      validateExistingMethods(psiClass, builder);
    }
    return result;
  }

  private boolean validateAnnotationOnRigthType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addError("@Convertable is only supported on a class or enum type");
      return false;
    }
    return true;
  }

  private boolean validateExistingMethods(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_FIELD_NAME, FROM_BEAN_FIELD_NAME)) {
      builder.addWarning("Not generated '%s'() or '%s'(): A method with same name already exists", TO_BEAN_FIELD_NAME, FROM_BEAN_FIELD_NAME);
      return false;
    }
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.addAll(createToBeanStringMethod(psiClass, psiAnnotation));
    target.addAll(createFromBeanStringMethod(psiClass, psiAnnotation));
  }

  private Collection<PsiMethod> createFromBeanStringMethod(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, FROM_BEAN_FIELD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(fromBeanStringMethod(psiClass));
  }

  @NotNull
  Collection<PsiMethod> createToBeanStringMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_FIELD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(toBeanStringMethod(psiClass));
  }

  private PsiMethod toBeanStringMethod(@NotNull PsiClass psiClass) {
    final PsiManager psiManager = psiClass.getManager();
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, TO_BEAN_FIELD_NAME)
      .withMethodReturnType(PsiType.getJavaLangClass(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withParameter("clazz", PsiType.getJavaLangClass(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withModifier(PsiModifier.PUBLIC);
    String blockText = String.format("return com.xyz.utils.JsonUtils.convert(this,%s)", psiClass.getQualifiedName());
    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

  private PsiMethod fromBeanStringMethod(@NotNull PsiClass psiClass) {
    final PsiManager psiManager = psiClass.getManager();
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, FROM_BEAN_FIELD_NAME)
      .withMethodReturnType(PsiType.getTypeByName(Objects.requireNonNull(psiClass.getQualifiedName()), psiManager.getProject(), GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withParameter("param", Bottom.BOTTOM)
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC);
    String blockText = String.format("return com.xyz.utils.JsonUtils.convert(param,%s)", psiClass.getQualifiedName());
    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

}
