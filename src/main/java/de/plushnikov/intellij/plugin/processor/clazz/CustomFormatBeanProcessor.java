package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiParameterImpl;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import de.plushnikov.intellij.plugin.util.PsiTypeUtil;
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
    PARAM_NAME = "param",
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
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_METHOD_NAME, 1) || PsiMethodUtil.hasMethodByName(classMethods, FROM_BEAN_METHOD_NAME, 0)) {
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
    if (PsiMethodUtil.hasMethodByName(classMethods, FROM_BEAN_METHOD_NAME, 0)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(fromBeanMethod(psiClass, psiAnnotation));
  }

  @NotNull
  Collection<PsiMethod> createToBeanMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_BEAN_METHOD_NAME, 1)) {
      return new ArrayList<>();
    }
    return Collections.singletonList(toBeanMethod(psiClass, psiAnnotation));
  }

  private PsiMethod toBeanMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    boolean hasBeanClass = PsiAnnotationUtil.hasDeclaredProperty(psiAnnotation, BEAN_ANNOTATION_NAME);
    if (hasBeanClass) {
      return toBeanMethodWithBeanClass(psiClass, psiAnnotation);
    } else {
      return toBeanMethodWithGeneric(psiClass);
    }
  }

  private PsiMethod toBeanMethodWithBeanClass(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    PsiType returnType = this.getAnnotatedBeanType(psiAnnotation);
    String beanClassName = PsiTypeUtil.getQualifiedName(returnType);

    LombokLightMethodBuilder builder = new LombokLightMethodBuilder(psiClass.getManager(), TO_BEAN_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC)
      .withContainingClass(psiClass)
      .withMethodReturnType(returnType);

    PsiCodeBlock block = PsiMethodUtil.createCodeBlockFromText(String.format("return com.xyz.utils.JsonUtils.convert(this, %s.class)", beanClassName), builder);
    builder.withBody(block);
    return builder;
  }

  private PsiType getAnnotatedBeanType(@NotNull PsiAnnotation psiAnnotation) {
    PsiAnnotationMemberValue attributeValue = psiAnnotation.findDeclaredAttributeValue(BEAN_ANNOTATION_NAME);
    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(psiAnnotation.getProject());
    return (PsiType) javaPsiFacade.getConstantEvaluationHelper().computeConstantExpression(attributeValue);
  }

  private PsiMethod toBeanMethodWithGeneric(@NotNull PsiClass psiClass) {
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

  private PsiMethod fromBeanMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    boolean hasBeanClass = PsiAnnotationUtil.hasDeclaredProperty(psiAnnotation, BEAN_ANNOTATION_NAME);
    if (hasBeanClass) {
      return fromBeanMethodWithBeanClass(psiClass, psiAnnotation);
    } else {
      return fromBeanMethodWithGeneric(psiClass);
    }
  }

  private PsiMethod fromBeanMethodWithBeanClass(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    PsiManager psiManager = psiClass.getManager();
    PsiType paramType = this.getAnnotatedBeanType(psiAnnotation);
    PsiClassType returnType = PsiType.getTypeByName(psiClass.getQualifiedName(), psiManager.getProject(), psiClass.getResolveScope());
    String returnClassName = PsiTypeUtil.getQualifiedName(returnType);

    LombokLightMethodBuilder builder = new LombokLightMethodBuilder(psiManager, FROM_BEAN_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC, PsiModifier.STATIC)
      .withParameter(PARAM_NAME, paramType)
      .withContainingClass(psiClass)
      .withMethodReturnType(returnType);

    PsiCodeBlock block = PsiMethodUtil.createCodeBlockFromText(String.format("return com.xyz.utils.JsonUtils.convert(%s, %s.class)", PARAM_NAME, returnClassName), builder);
    builder.withBody(block);
    return builder;
  }

  private PsiMethod fromBeanMethodWithGeneric(@NotNull PsiClass psiClass) {
    PsiManager psiManager = psiClass.getManager();
    PsiClassType returnType = PsiType.getTypeByName(psiClass.getQualifiedName(), psiManager.getProject(), psiClass.getResolveScope());
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiManager.getProject());
    PsiMethod methodFromText = elementFactory.createMethodFromText(FROM_BEAN_METHOD, null);

    LombokLightMethodBuilder builder = new LombokLightMethodBuilder(psiManager, FROM_BEAN_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC, PsiModifier.STATIC)
      .withTypeParameter(methodFromText.getTypeParameters()[0])
      .withParameter((PsiParameterImpl) methodFromText.getParameters()[0])
      .withContainingClass(psiClass)
      .withMethodReturnType(returnType);

    PsiCodeBlock block = PsiMethodUtil.createCodeBlockFromText(String.format("return com.xyz.utils.JsonUtils.convert(param,%s.class)", psiClass.getQualifiedName()), builder);
    builder.withBody(block);
    return builder;
  }
}
