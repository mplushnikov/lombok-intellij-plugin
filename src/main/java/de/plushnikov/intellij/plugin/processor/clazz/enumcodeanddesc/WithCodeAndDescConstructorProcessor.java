package de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc;

import com.intellij.psi.*;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.WithCodeAndDesc;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class WithCodeAndDescConstructorProcessor extends AbstractWithCodeAndDescProcessor {

  public WithCodeAndDescConstructorProcessor() {
    super(PsiMethod.class, WithCodeAndDesc.class);
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return validateExistingConstructor(psiClass, builder);
  }


  private boolean validateExistingConstructor(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    return true;
  }

  @Override
  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    PsiField codeField = this.findCodeField(psiClass, psiAnnotation);
    PsiField descField = this.findDescField(psiClass, psiAnnotation);

    target.add(createConstructor(psiClass, psiAnnotation, codeField));
    target.add(createConstructor(psiClass, psiAnnotation, codeField, descField));
  }

  private PsiMethod createConstructor(PsiClass psiClass, PsiAnnotation psiAnnotation, PsiField... fields) {
    LombokLightMethodBuilder constructorBuilder = new LombokLightMethodBuilder(psiClass.getManager(), Objects.requireNonNull(psiClass.getName()))
      .withConstructor(true)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PUBLIC);

    final List<String> fieldNames = new ArrayList<>();
    final AccessorsInfo classAccessorsInfo = AccessorsInfo.build(psiClass);
    for (PsiField psiField : fields) {
      final AccessorsInfo paramAccessorsInfo = AccessorsInfo.build(psiField, classAccessorsInfo);
      fieldNames.add(paramAccessorsInfo.removePrefix(psiField.getName()));
    }

    if (!fieldNames.isEmpty()) {
      boolean addConstructorProperties = configDiscovery.getBooleanLombokConfigProperty(ConfigKey.ANYCONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES, psiClass);
      if (addConstructorProperties || !configDiscovery.getBooleanLombokConfigProperty(ConfigKey.ANYCONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES, psiClass)) {
        final String constructorPropertiesAnnotation = "java.beans.ConstructorProperties( {" +
          fieldNames.stream().collect(Collectors.joining("\", \"", "\"", "\"")) +
          "} ) ";
        constructorBuilder.withAnnotation(constructorPropertiesAnnotation);
      }
    }

    constructorBuilder.withAnnotations(LombokProcessorUtil.getOnX(psiAnnotation, "onConstructor"));


    final Iterator<String> fieldNameIterator = fieldNames.iterator();
    final Iterator<PsiField> fieldIterator = Arrays.asList(fields).iterator();
    while (fieldNameIterator.hasNext() && fieldIterator.hasNext()) {
      constructorBuilder.withParameter(fieldNameIterator.next(), fieldIterator.next().getType());
    }

    final StringBuilder blockText = new StringBuilder();

    final Iterator<String> fieldNameIt = fieldNames.iterator();
    final Iterator<PsiField> fieldIt = Arrays.asList(fields).iterator();
    while (fieldNameIt.hasNext() && fieldIt.hasNext()) {
      final PsiField param = fieldIt.next();
      final String fieldName = fieldNameIt.next();
      blockText.append(String.format("this.%s = %s;\n", param.getName(), fieldName));
    }

    constructorBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText.toString(), constructorBuilder));

    return constructorBuilder;
  }
}
