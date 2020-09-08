package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiParameterImpl;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.Convertable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @JsonSerializable lombok annotation on a class
 * Creates toBean() and fromBean() method for fields of this class
 *
 * @author lihongbin
 */
public class CustomFormatBeanProcessor extends AbstractClassProcessor {

  private static final String TO_BEAN_METHOD_NAME = "toBean",
    FROM_BEAN_METHOD_NAME = "fromBean",
    BEAN_ANNOTATION_NAME = "bean",
    TO_BEAN_FUNCTION = "public <T> T toBean(Class<T> clazz) {\n" +
      "        return JsonUtils.convert(this, clazz);\n" +
      "    }",
    FROM_BEAN_METHOD = "public static <T> statement fromBean(T param) {\n" +
      "        return  JsonUtils.convert(param, Statement.class);\n" +
      "    }";

  public CustomFormatBeanProcessor() {
    super(PsiMethod.class, Convertable.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final boolean result = validateAnnotationOnRightType(psiClass, builder);
    if (result) {
      validateExistingMethods(psiClass, builder);
    }
    boolean hasBeanClass = PsiAnnotationUtil.hasDeclaredProperty(psiAnnotation, BEAN_ANNOTATION_NAME);
    builder.addWarning(String.format("bean class is provided: %b", hasBeanClass));
    return result;
  }

  private boolean validateAnnotationOnRightType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addError("@Convertable is only supported on a class or enum type");
      return false;
    }
    return true;
  }

  private void validateExistingMethods(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_METHOD_NAME, FROM_BEAN_METHOD_NAME)) {
      builder.addWarning("Not generated '%s'() or '%s'(): A method with same name already exists", TO_BEAN_METHOD_NAME, FROM_BEAN_METHOD_NAME);
    }
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.addAll(createToBeanMethod(psiClass, psiAnnotation));
    target.addAll(createFromBeanMethod(psiClass, psiAnnotation));
  }

  private Collection<PsiMethod> createFromBeanMethod(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, FROM_BEAN_METHOD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(fromBeanStringMethod(psiClass));
  }

  @NotNull
  Collection<PsiMethod> createToBeanMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_METHOD_NAME)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(toBeanStringMethod(psiClass));
  }

  private PsiMethod toBeanStringMethod(@NotNull PsiClass psiClass) {
    PsiManager psiManager = psiClass.getManager();
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiManager.getProject());
    PsiMethod methodFromText = elementFactory.createMethodFromText(TO_BEAN_FUNCTION, null);

    return new LombokLightMethodBuilder(psiManager, TO_BEAN_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC)
      .withTypeParameter(methodFromText.getTypeParameters()[0])
      .withParameter((PsiParameterImpl) methodFromText.getParameters()[0])
      .withContainingClass(psiClass)
      .withMethodReturnType(methodFromText.getReturnType())
      .withBody(methodFromText.getBody());
  }

  private PsiMethod fromBeanStringMethod(@NotNull PsiClass psiClass) {
    PsiManager psiManager = psiClass.getManager();
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiManager.getProject());
    PsiMethod methodFromText = elementFactory.createMethodFromText(FROM_BEAN_METHOD, null);

    LombokLightMethodBuilder buildMethod = new LombokLightMethodBuilder(psiManager, FROM_BEAN_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC, PsiModifier.STATIC)
      .withTypeParameter(methodFromText.getTypeParameters()[0])
      .withParameter((PsiParameterImpl) methodFromText.getParameters()[0])
      .withContainingClass(psiClass)
      .withMethodReturnType(PsiType.getTypeByName(psiClass.getQualifiedName(), psiManager.getProject(), psiClass.getResolveScope()));
    buildMethod.withBody(PsiMethodUtil.createCodeBlockFromText(
      String.format("return com.xyz.utils.JsonUtils.convert(param,%s)", psiClass.getQualifiedName() + ".class")
      , buildMethod)
    );
    return buildMethod;
  }

}
