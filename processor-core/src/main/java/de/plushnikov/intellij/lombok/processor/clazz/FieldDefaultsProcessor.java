  package de.plushnikov.intellij.lombok.processor.clazz;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import de.plushnikov.intellij.lombok.LombokUtils;
import de.plushnikov.intellij.lombok.UserMapKeys;
import de.plushnikov.intellij.lombok.problem.ProblemBuilder;
import de.plushnikov.intellij.lombok.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.lombok.psi.LombokPsiElementFactory;
import de.plushnikov.intellij.lombok.util.LombokProcessorUtil;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;
import org.jetbrains.annotations.NotNull;

/**
 * Inspect and validate @FieldDefaults lombok annotation on a field.
 *
 * @author William Delanoue
 */
public class FieldDefaultsProcessor extends AbstractLombokClassProcessor {
  private static final Logger LOG = Logger.getLogger(FieldDefaultsProcessor.class.getSimpleName());

  // Sorry * 1000, but, in "PsiField" generator, a PsiClass.getFields() recall "processIntern". How to avoid this ?!
  private Set<PsiClass> inUse = new ConcurrentSet<PsiClass>();

  public FieldDefaultsProcessor() {
    super(FieldDefaults.class, PsiField.class);
  }

  @Override
  protected void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    if (inUse.contains(psiClass)) {
      return;
    }
    inUse.add(psiClass);
    try {
      final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation, "level");
      final Boolean mustBeEqual = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "makeFinal", Boolean.class);
      target.addAll(recreateFields(psiClass, methodVisibility, Boolean.TRUE.equals(mustBeEqual)));
    } finally {
      inUse.remove(psiClass);
    }
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final boolean result = validateAnnotationOnRigthType(psiClass, builder);

    return result;
  }

  protected boolean validateAnnotationOnRigthType(@NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addError("'@Getter' is only supported on a class, enum or field type");
      result = false;
    }
    return result;
  }

  @NotNull
  public Collection<PsiField> recreateFields(@NotNull PsiClass psiClass, @NotNull String methodModifier, boolean mustBeFinal) {
    Collection<PsiField> result = new ArrayList<PsiField>();

    for (PsiField psiField : psiClass.getFields()) {
      boolean createGetter = true;
      PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        //Skip static fields.
//        createGetter = !modifierList.hasModifierProperty(PsiModifier.STATIC);
        //Skip fields having Getter annotation already
//        createGetter &= !hasFieldProcessorAnnotation(modifierList);
        //Skip fields that start with $
        createGetter &= !psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);
        //Skip fields if a method with same name and arguments count already exists
//        final Collection<String> methodNames = LombokUtils.toAllGetterNames(psiField.getName(), PsiType.BOOLEAN.equals(psiField.getType()));
//        for (String methodName : methodNames) {
//          createGetter &= !PsiMethodUtil.hasSimilarMethod(classMethods, methodName, 0);
//        }
      }
      if (createGetter) {
        result.add(recreateField(psiField, methodModifier, mustBeFinal));
      }
    }
    return result;
  }

  @NotNull
  public PsiField recreateField(@NotNull PsiField psiField, @NotNull String modifier, boolean mustBeFinal) {
    PsiClass psiClass = psiField.getContainingClass();
    assert psiClass != null;

    boolean mustBePrivate = PsiAnnotationUtil.isAnnotatedWith(psiField, PackagePrivate.class);

    UserMapKeys.addReadUsageFor(psiField);

    LombokLightFieldBuilder field = LombokPsiElementFactory.getInstance().createLightField(psiField.getManager(), psiField.getName(), psiField.getType())
        .withContainingClass(psiClass)
//        .withModifier(modifier)
        .withNavigationElement(psiField);

//    LombokLightMethodBuilder method = LombokPsiElementFactory.getInstance().createLightMethod(psiField.getManager(), methodName)
//        .withMethodReturnType(psiField.getType())
//        .withContainingClass(psiClass)
//        .withNavigationElement(psiField);
    if (!StringUtil.isNotEmpty(modifier)) {
      field.withModifier(modifier);
    }
    if (mustBePrivate) {
      field.withModifier(PsiModifier.PRIVATE);
    }

    if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
      field.withModifier(PsiModifier.STATIC);
    }
    if (mustBeFinal && !PsiAnnotationUtil.isAnnotatedWith(psiField, NonFinal.class)) {
      field.withModifier(PsiModifier.FINAL);
    }

//    copyAnnotations(psiField, field.getModifierList(), Pattern.compile(".*"),
//        LombokUtils.NON_NULL_PATTERN, LombokUtils.NULLABLE_PATTERN, LombokUtils.DEPRECATED_PATTERN);
    return field;
  }

}
