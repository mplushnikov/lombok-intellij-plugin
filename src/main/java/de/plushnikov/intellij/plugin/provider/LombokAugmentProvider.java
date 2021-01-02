package de.plushnikov.intellij.plugin.provider;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import de.plushnikov.intellij.plugin.processor.LombokProcessorManager;
import de.plushnikov.intellij.plugin.processor.Processor;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.ModifierProcessor;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Provides support for lombok generated elements
 *
 * @author Plushnikov Michail
 */
public class LombokAugmentProvider extends PsiAugmentProvider {
  private static final Logger log = Logger.getInstance(LombokAugmentProvider.class.getName());

  private final ValProcessor valProcessor;
  private final Collection<ModifierProcessor> modifierProcessors;

  public LombokAugmentProvider() {
    log.debug("LombokAugmentProvider created");

    modifierProcessors = LombokProcessorManager.getLombokModifierProcessors();
    valProcessor = ServiceManager.getService(ValProcessor.class);
  }

  @NotNull
  @Override
  protected Set<String> transformModifiers(@NotNull PsiModifierList modifierList, @NotNull final Set<String> modifiers) {
    // make copy of original modifiers
    Set<String> result = new HashSet<>(modifiers);

    // Loop through all available processors and give all of them a chance to respond
    for (ModifierProcessor processor : modifierProcessors) {
      if (processor.isSupported(modifierList)) {
        processor.transformModifiers(modifierList, result);
      }
    }

    return result;
  }

  @Nullable
  @Override
  protected PsiType inferType(@NotNull PsiTypeElement typeElement) {
    return valProcessor.inferType(typeElement);
  }

  @NotNull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element,
                                                        @NotNull final Class<Psi> type) {
    return getAugments(element, type, null);
  }

  @NotNull
//  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element,
                                                        @NotNull final Class<Psi> type,
                                                        @Nullable String nameHint) {
    final List<Psi> emptyResult = Collections.emptyList();
    if ((type != PsiClass.class && type != PsiField.class && type != PsiMethod.class) || !(element instanceof PsiExtensibleClass)) {
      return emptyResult;
    }

    final PsiClass psiClass = (PsiClass) element;
    // Skip processing of Annotations and Interfaces
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      return emptyResult;
    }
    // skip processing if disabled, or no lombok library is present
    if (!LombokLibraryUtil.hasLombokLibrary(element.getProject())) {
      return emptyResult;
    }

    final LombokValueProvider<Psi> result;
    if (type == PsiField.class) {
      result = new FieldLombokProvider<>(type, psiClass, nameHint);
    } else if (type == PsiMethod.class) {
      result = new MethodLombokProvider<>(type, psiClass, nameHint);
    } else {
      result = new ClassLombokProvider<>(type, psiClass, nameHint);
    }

    final List<Psi> computed = result.compute();
    return null != computed ? computed : emptyResult;
  }

  private static class FieldLombokProvider<Psi extends PsiElement> extends LombokValueProvider<Psi> {
    private static final RecursionGuard<PsiClass> ourGuard = RecursionManager.createGuard("lombok.augment.field");

    FieldLombokProvider(Class<Psi> type, PsiClass psiClass, String nameHint) {
      super(type, psiClass, ourGuard, nameHint);
    }
  }

  private static class MethodLombokProvider<Psi extends PsiElement> extends LombokValueProvider<Psi> {
    private static final RecursionGuard<PsiClass> ourGuard = RecursionManager.createGuard("lombok.augment.method");

    MethodLombokProvider(Class<Psi> type, PsiClass psiClass, String nameHint) {
      super(type, psiClass, ourGuard, nameHint);
    }
  }

  private static class ClassLombokProvider<Psi extends PsiElement> extends LombokValueProvider<Psi> {
    private static final RecursionGuard<PsiClass> ourGuard = RecursionManager.createGuard("lombok.augment.class");

    ClassLombokProvider(Class<Psi> type, PsiClass psiClass, String nameHint) {
      super(type, psiClass, ourGuard, nameHint);
    }
  }

  private abstract static class LombokValueProvider<Psi extends PsiElement> {
    private final Class<Psi> type;
    private final PsiClass psiClass;
    private final RecursionGuard<PsiClass> recursionGuard;
    private final String nameHint;

    LombokValueProvider(Class<Psi> type, PsiClass psiClass, RecursionGuard<PsiClass> recursionGuard, String nameHint) {
      this.type = type;
      this.psiClass = psiClass;
      this.recursionGuard = recursionGuard;
      this.nameHint = nameHint;
    }

    public List<Psi> compute() {
      return recursionGuard.doPreventingRecursion(psiClass, true, this::computeIntern);
    }

    private List<Psi> computeIntern() {
      return getPsis(psiClass, type, nameHint);
    }
  }

  @NotNull
  private static <Psi extends PsiElement> List<Psi> getPsis(PsiClass psiClass, Class<Psi> type, String nameHint) {
    final List<Psi> result = new ArrayList<>();
    final Collection<Processor> lombokProcessors = LombokProcessorProvider.getInstance(psiClass.getProject()).getLombokProcessors(type);
    for (Processor processor : lombokProcessors) {
      if (processor.notNameHintIsEqualToSupportedAnnotation(nameHint)) {
        final List<? super PsiElement> generatedElements = processor.process(psiClass, nameHint);
        for (Object psiElement : generatedElements) {
          result.add((Psi) psiElement);
        }
      }
    }
    return result;
  }
}
