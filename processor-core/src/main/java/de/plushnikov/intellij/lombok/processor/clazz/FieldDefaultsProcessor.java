package de.plushnikov.intellij.lombok.processor.clazz;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.util.containers.ConcurrentHashSet;
import de.plushnikov.intellij.lombok.LombokUtils;
import de.plushnikov.intellij.lombok.UserMapKeys;
import de.plushnikov.intellij.lombok.problem.ProblemBuilder;
import de.plushnikov.intellij.lombok.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.lombok.psi.LombokPsiElementFactory;
import de.plushnikov.intellij.lombok.util.LombokProcessorUtil;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import de.plushnikov.intellij.lombok.util.PsiClassUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import lombok.AccessLevel;
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

  public FieldDefaultsProcessor() {
    super(FieldDefaults.class, PsiField.class);
  }

  @Override
  protected void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation, "level");
    final Boolean makeFinal = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "makeFinal", Boolean.class);
    target.addAll(recreateFields(psiClass, methodVisibility, Boolean.TRUE.equals(makeFinal)));
  }

  @Override
  protected boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass, @NotNull ProblemBuilder builder) {
    final boolean result = validateAnnotationOnRigthType(psiClass, builder);

    if (result) {
      final AccessLevel level = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "level", AccessLevel.class);
      final boolean makeFinal = Boolean.TRUE.equals(PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "makeFinal", Boolean.class));
      if (level == AccessLevel.NONE && !makeFinal) {
        builder.addError("This does nothing; provide either level or makeFinal or both.");
        return false;
      }

      if (level == AccessLevel.PACKAGE) {
        builder.addWarning("Setting 'level' to PACKAGE does nothing. To force fields as package private, use the @PackagePrivate annotation on the field.");
      }

      if (!makeFinal && PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "makeFinal", Boolean.class) != null) {
        builder.addWarning("Setting 'makeFinal' to false does nothing. To force fields to be non-final, use the @NonFinal annotation on the field.");
      }
    }
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
  public Collection<PsiField> recreateFields(@NotNull PsiClass psiClass, String methodModifier, boolean mustBeFinal) {
    Collection<PsiField> result = new ArrayList<PsiField>();

    for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
      if (!psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER)) {
        PsiField newField = recreateField(psiField, methodModifier, mustBeFinal);
        if (newField != null) {
          result.add(newField);
        }
      }
    }
    return result;
  }

  //  @NotNull
  public PsiField recreateField(@NotNull final PsiField psiField, final String modifier, final boolean mustBeFinal) {
    PsiClass psiClass = psiField.getContainingClass();
    assert psiClass != null;

    final boolean mustBePrivate = PsiAnnotationUtil.isAnnotatedWith(psiField, PackagePrivate.class);

//    UserMapKeys.addWriteUsageFor(psiField);
    UserMapKeys.addReadUsageFor(psiField);

    final LombokLightFieldBuilder field = LombokPsiElementFactory.getInstance().createLightField(psiField.getManager(), psiField.getName(), psiField.getType())
        .withContainingClass(psiClass);
    if (mustBePrivate) {
      field.withModifier(PsiModifier.PRIVATE);
    } else if (modifier != null) {
      field.withModifier(modifier);
    }
    if (psiField.hasInitializer()) {
      field.setInitializer(field.getInitializer());
    }

    if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
      field.withModifier(PsiModifier.STATIC);
    }
    if (mustBeFinal && !PsiAnnotationUtil.isAnnotatedWith(psiField, NonFinal.class)) {
      field.withModifier(PsiModifier.FINAL);
    }

    ApplicationManager.getApplication().invokeLater(
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              @Override
              public void run() {
                if (mustBePrivate) {
                  psiField.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
                } else if (modifier != null) {
                  psiField.getModifierList().setModifierProperty(modifier, true);
                } else {
                  psiField.getModifierList().setModifierProperty(PsiModifier.PACKAGE_LOCAL, true);
                }
                psiField.getModifierList().setModifierProperty(PsiModifier.FINAL, mustBeFinal && !PsiAnnotationUtil.isAnnotatedWith(psiField, NonFinal.class));
              }
            });
          }
        }
    );

//    psiField.delete();
//    copyAnnotations(psiField, field.getModifierList(), Pattern.compile(".*"),
//        LombokUtils.NON_NULL_PATTERN, LombokUtils.NULLABLE_PATTERN, LombokUtils.DEPRECATED_PATTERN);
//    return field;
    return null;
  }


}
