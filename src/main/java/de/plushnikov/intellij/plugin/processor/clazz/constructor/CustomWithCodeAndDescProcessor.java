package de.plushnikov.intellij.plugin.processor.clazz.constructor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.WithCodeAndDesc;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Inspect and validate @WithCodeAndDesc lombok annotation on a class
 * Creates code, desc fields, corresponding constructors and of() method
 */
public class CustomWithCodeAndDescProcessor extends AbstractConstructorClassProcessor {
  private static final String CODE_FIELD_NAME = "codeName";
  private static final String DESC_FIELD_NAME = "descName";
  private static final String OF_METHOD_NAME = "of";

  public CustomWithCodeAndDescProcessor() {
    super(WithCodeAndDesc.class, PsiMethod.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return super.validate(psiAnnotation, psiClass, builder);
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    PsiField codeField = genCodeField(psiClass, psiAnnotation);
    PsiField descField = genDescField(psiClass, psiAnnotation);
    target.add(codeField);
    target.add(descField);
    target.addAll(createConstructor(psiClass, psiAnnotation, codeField));
    target.addAll(createConstructor(psiClass, psiAnnotation, codeField, descField));
    target.addAll(createGetMethods(psiClass, codeField, descField));
    target.add(createOfMethod(psiClass, codeField));
  }

  private PsiField genCodeField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    PsiManager psiManager = psiClass.getManager();

    return new LombokLightFieldBuilder(psiManager, getAnnotatedValue(psiAnnotation, CODE_FIELD_NAME), PsiType.INT)
      .withModifier(PsiModifier.PRIVATE);
  }

  private PsiField genDescField(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    PsiManager psiManager = psiClass.getManager();

    return new LombokLightFieldBuilder(psiManager, getAnnotatedValue(psiAnnotation, DESC_FIELD_NAME), PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withModifier(PsiModifier.PRIVATE);
  }

  private Collection<PsiMethod> createConstructor(PsiClass psiClass, PsiAnnotation psiAnnotation, PsiField... fields) {
    return createConstructorMethod(psiClass, PsiModifier.PUBLIC, psiAnnotation, false, Arrays.asList(fields.clone()), null, false);
  }

  private Collection<PsiMethod> createGetMethods(PsiClass psiClass, PsiField... fields) {
    Collection<PsiMethod> result = new ArrayList<>();
    GetterFieldProcessor fieldProcessor = getGetterFieldProcessor();
    for (PsiField getterField : fields) {
      result.add(fieldProcessor.createGetterMethod(getterField, psiClass, PsiModifier.PUBLIC));
    }
    return result;
  }

  private PsiMethod createOfMethod(PsiClass psiClass, PsiField codeField) {
    final PsiManager psiManager = psiClass.getManager();
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, OF_METHOD_NAME)
      .withMethodReturnType(PsiType.getTypeByName(Objects.requireNonNull(psiClass.getQualifiedName()), psiManager.getProject(), GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withParameter(codeField.getName(), PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC);

    String blockText = String.format("return Arrays.stream(%s.values())"
      +".filter(x -> code == x.code)"
      +".findAny()"
      +".orElseThrow(() -> new IllegalArgumentException(\"Unknown code value, please check again\"))", psiClass.getQualifiedName());

    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

  private String getAnnotatedValue(@NotNull PsiAnnotation psiAnnotation, @NotNull String attrName) {
    PsiAnnotationMemberValue attributeValue = psiAnnotation.findDeclaredAttributeValue(attrName);
    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(psiAnnotation.getProject());
    return (String) javaPsiFacade.getConstantEvaluationHelper().computeConstantExpression(attributeValue);
  }

  private GetterFieldProcessor getGetterFieldProcessor() {
    return ServiceManager.getService(GetterFieldProcessor.class);
  }
}
