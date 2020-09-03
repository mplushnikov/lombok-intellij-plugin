package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Inspect and validate @JsonSerializable lombok annotation on a class
 * Creates toJson() and fromJson() method for fields of this class
 *
 * @author lihongbin
 */
public class CustomJsonSerializableProcessor extends AbstractClassProcessor {

  public static final String TO_JSON_METHOD_NAME = "toJson",
    FROM_JSON_METHOD_NAME = "fromJson";

  public CustomJsonSerializableProcessor() {
    super(PsiMethod.class, JsonSerializable.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final boolean result = validateAnnotationOnRightType(psiClass, builder);
    if (result) {
      validateExistingMethods(psiClass, builder);
    }
    return result;
  }

  private boolean validateAnnotationOnRightType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addError("@JsonSerializable is only supported on a class or enum type");
      return false;
    }
    return true;
  }

  private boolean validateExistingMethods(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_JSON_METHOD_NAME, FROM_JSON_METHOD_NAME)) {
      builder.addWarning("Not generated '%s'() or '%s'(): A method with same name already exists", TO_JSON_METHOD_NAME, FROM_JSON_METHOD_NAME);
      return false;
    }
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.addAll(createToJsonMethod(psiClass, psiAnnotation));
    target.addAll(createFromJsonMethod(psiClass, psiAnnotation));
  }

  private Collection<PsiMethod> createFromJsonMethod(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, FROM_JSON_METHOD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(fromJsonMethod(psiClass));
  }

  @NotNull
  Collection<PsiMethod> createToJsonMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_JSON_METHOD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(toJsonMethod(psiClass));
  }

  private PsiMethod toJsonMethod(@NotNull PsiClass psiClass) {
    final PsiManager psiManager = psiClass.getManager();
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, TO_JSON_METHOD_NAME)
      .withMethodReturnType(PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withModifier(PsiModifier.PUBLIC);
    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText("return com.xyz.utils.JsonUtils.beanToJson(this)", methodBuilder));
    return methodBuilder;
  }

  private PsiMethod fromJsonMethod(@NotNull PsiClass psiClass) {
    final PsiManager psiManager = psiClass.getManager();
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, FROM_JSON_METHOD_NAME)
      .withMethodReturnType(PsiType.getTypeByName(Objects.requireNonNull(psiClass.getQualifiedName()), psiManager.getProject(), GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withParameter("jsonStr", PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC);
    String blockText = String.format("return com.xyz.utils.JsonUtils.jsonToBean(jsonStr,%s)", psiClass.getQualifiedName());
    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

}
