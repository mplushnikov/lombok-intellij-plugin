package de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.WithCodeAndDesc;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class WithCodeAndDescMethodProcessor extends AbstractWithCodeAndDescProcessor {
  private static final String OF_METHOD_NAME = "of";

  public WithCodeAndDescMethodProcessor() {
    super(PsiMethod.class, WithCodeAndDesc.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    PsiField codeField = this.findCodeField(psiClass, psiAnnotation);
    PsiField descField = this.findDescField(psiClass, psiAnnotation);

    target.addAll(createGetMethods(psiClass, codeField, descField));
    target.add(createOfMethod(psiClass, codeField));
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
      .withParameter(codeField.getName(), PsiType.INT)
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC);

    String blockText = String.format("return Arrays.stream(%s.values())"
      + ".filter(x -> code == x.code)"
      + ".findAny()"
      + ".orElseThrow(() -> new IllegalArgumentException(\"Unknown code value, please check again\"))", psiClass.getQualifiedName());

    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

  private GetterFieldProcessor getGetterFieldProcessor() {
    return ServiceManager.getService(GetterFieldProcessor.class);
  }
}
