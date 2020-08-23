package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemBuilder;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.handler.EqualsAndHashCodeToStringHandler;
import de.plushnikov.intellij.plugin.processor.handler.EqualsAndHashCodeToStringHandler.MemberInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import lombok.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @JsonSerializable lombok annotation on a class
 * Creates toJson() and fromJson() method for fields of this class
 *
 * @author lihongbin
 */
public class CustomJsonSerializableProcessor extends AbstractClassProcessor {

  public static final String TO_JSON_METHOD_NAME = "toJson",
    FROM_JSON_METHOD_NAME = "fromJson";

  private static final String INCLUDE_ANNOTATION_METHOD = "name";

  public CustomJsonSerializableProcessor() {
    super(PsiMethod.class, JsonSerializable.class);
  }

  private EqualsAndHashCodeToStringHandler getEqualsAndHashCodeToStringHandler() {
    return ServiceManager.getService(EqualsAndHashCodeToStringHandler.class);
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

  protected void generatePsiElements(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    target.addAll(createToJsonStringMethod(psiClass, psiAnnotation));
    target.addAll(createFromJsonStringMethod(psiClass, psiAnnotation));
  }

  private Collection<PsiMethod> createFromJsonStringMethod(PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, FROM_JSON_METHOD_NAME)) {
      return new ArrayList<>();
    }

    final Collection<MemberInfo> memberInfos = getEqualsAndHashCodeToStringHandler().filterFields(psiClass, psiAnnotation, false, INCLUDE_ANNOTATION_METHOD);
    final PsiMethod stringMethod = createToStringMethod(psiClass, memberInfos, psiAnnotation, false);
    return Collections.singletonList(stringMethod);
  }

  @NotNull
  Collection<PsiMethod> createToJsonStringMethod(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (PsiMethodUtil.hasMethodByName(classMethods, TO_JSON_METHOD_NAME)) {
      return new ArrayList<>();
    }

    final Collection<MemberInfo> memberInfos = getEqualsAndHashCodeToStringHandler().filterFields(psiClass, psiAnnotation, false, INCLUDE_ANNOTATION_METHOD);
    final PsiMethod stringMethod = createToStringMethod(psiClass, memberInfos, psiAnnotation, false);
    return Collections.singletonList(stringMethod);
  }

  @NotNull
  public PsiMethod createToStringMethod(@NotNull PsiClass psiClass, @NotNull Collection<MemberInfo> memberInfos, @NotNull PsiAnnotation psiAnnotation, boolean forceCallSuper) {
    final PsiManager psiManager = psiClass.getManager();

    final String paramString = createParamString(psiClass, memberInfos, psiAnnotation, forceCallSuper);
    final String blockText = String.format("return \"%s(%s)\";", getSimpleClassName(psiClass), paramString);

    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, TO_JSON_METHOD_NAME)
      .withMethodReturnType(PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(psiClass.getProject())))
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PUBLIC);
    methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
    return methodBuilder;
  }

  private String getSimpleClassName(@NotNull PsiClass psiClass) {
    final StringBuilder psiClassName = new StringBuilder();

    PsiClass containingClass = psiClass;
    do {
      if (psiClassName.length() > 0) {
        psiClassName.insert(0, '.');
      }
      psiClassName.insert(0, containingClass.getName());
      containingClass = containingClass.getContainingClass();
    } while (null != containingClass);

    return psiClassName.toString();
  }

  private String createParamString(@NotNull PsiClass psiClass, @NotNull Collection<MemberInfo> memberInfos, @NotNull PsiAnnotation psiAnnotation, boolean forceCallSuper) {
    final boolean callSuper = forceCallSuper || readCallSuperAnnotationOrConfigProperty(psiAnnotation, psiClass, ConfigKey.TOSTRING_CALL_SUPER);
    final boolean doNotUseGetters = readAnnotationOrConfigProperty(psiAnnotation, psiClass, "doNotUseGetters", ConfigKey.TOSTRING_DO_NOT_USE_GETTERS);
    final boolean includeFieldNames = readAnnotationOrConfigProperty(psiAnnotation, psiClass, "includeFieldNames", ConfigKey.TOSTRING_INCLUDE_FIELD_NAMES);

    final StringBuilder paramString = new StringBuilder();
    if (callSuper) {
      paramString.append("super=\" + super.toString() + \", ");
    }

    for (MemberInfo memberInfo : memberInfos) {

      if (includeFieldNames) {
        paramString.append(memberInfo.getName()).append('=');
      }
      paramString.append("\"+");

      final PsiType classFieldType = memberInfo.getType();
      if (classFieldType instanceof PsiArrayType) {
        final PsiType componentType = ((PsiArrayType) classFieldType).getComponentType();
        if (componentType instanceof PsiPrimitiveType) {
          paramString.append("java.util.Arrays.toString(");
        } else {
          paramString.append("java.util.Arrays.deepToString(");
        }
      }

      final String memberAccessor = getEqualsAndHashCodeToStringHandler().getMemberAccessorName(memberInfo, doNotUseGetters, psiClass);
      paramString.append("this.").append(memberAccessor);

      if (classFieldType instanceof PsiArrayType) {
        paramString.append(")");
      }

      paramString.append("+\", ");
    }
    if (paramString.length() > 2) {
      paramString.delete(paramString.length() - 2, paramString.length());
    }
    return paramString.toString();
  }


  @Override
  public LombokPsiElementUsage checkFieldUsage(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      final String psiFieldName = StringUtil.notNullize(psiField.getName());
      if (getEqualsAndHashCodeToStringHandler().filterFields(containingClass, psiAnnotation, false, INCLUDE_ANNOTATION_METHOD).stream()
        .map(MemberInfo::getName).anyMatch(psiFieldName::equals)) {
        return LombokPsiElementUsage.READ;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
