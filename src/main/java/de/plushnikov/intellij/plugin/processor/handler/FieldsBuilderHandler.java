package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Handler methods for Builder-processing
 *
 * @author Tomasz Kalkosiński
 * @author Michail Plushnikov
 */
public class FieldsBuilderHandler {
  public FieldsBuilderHandler() {
  }
  public boolean notExistInnerClass(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    return !getExistInnerBuilderClass(psiClass).isPresent();
  }
  private Optional<PsiClass> getExistInnerBuilderClass(@NotNull PsiClass psiClass) {
    final String builderClassName = getBuilderClassName();
    return PsiClassUtil.getInnerClassInternByName(psiClass, builderClassName);
  }

  @NotNull
  public String getBuilderClassName() {
    return "Fields";
  }
  @NotNull
  public PsiClass createBuilderClass(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    LombokLightClassBuilder builderClass = createEmptyBuilderClass(psiClass, psiAnnotation);
    for (PsiField field : psiClass.getFields()) {
      builderClass.withField(createFieldNameConstant(field,psiClass,psiAnnotation));
    }
    return builderClass;
  }
  @NotNull
  private LombokLightClassBuilder createEmptyBuilderClass(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final String builderClassName = getBuilderClassName();
    final String builderClassQualifiedName = psiClass.getQualifiedName() + "." + builderClassName;
    return new LombokLightClassBuilder(psiClass, builderClassName, builderClassQualifiedName)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC)
      .withModifier(PsiModifier.FINAL);
  }


  @NotNull
  public PsiField createFieldNameConstant(@NotNull PsiField psiField, @NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final PsiManager manager = psiClass.getContainingFile().getManager();
    final PsiType psiFieldType = PsiType.getJavaLangString(manager, GlobalSearchScope.allScope(psiClass.getProject()));
    String fieldName = psiField.getName();
    LombokLightFieldBuilder fieldNameConstant = new LombokLightFieldBuilder(manager, fieldName, psiFieldType)
      .withContainingClass(psiClass)
      .withNavigationElement(psiField)
      .withModifier(PsiModifier.PUBLIC)
      .withModifier(PsiModifier.STATIC)
      .withModifier(PsiModifier.FINAL);
    final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiExpression initializer = psiElementFactory.createExpressionFromText("\"" + fieldName + "\"", psiClass);
    fieldNameConstant.setInitializer(initializer);
    return fieldNameConstant;
  }
}
