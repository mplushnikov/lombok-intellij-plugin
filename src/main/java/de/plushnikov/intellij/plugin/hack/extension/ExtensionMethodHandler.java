package de.plushnikov.intellij.plugin.hack.extension;

import com.google.common.collect.MapMaker;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.processor.MethodResolverProcessor;
import com.intellij.psi.util.*;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightParameter;
import de.plushnikov.intellij.plugin.settings.ProjectSettings;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtensionMethodHandler {

  public static boolean isEnabled(@NotNull Project project) {
    return ProjectSettings.isEnabled(project, ProjectSettings.IS_EXTENSION_METHOD_ENABLED);
  }

  public static boolean processDeclarations(final boolean result, final PsiClass psiClass, final PsiScopeProcessor processor, final ResolveState state, final PsiElement lastParent, final PsiElement place) {
    if (!result)
      return false;
    // lombok does not support extensions for method references.
    if (place instanceof PsiMethodReferenceExpression)
      return result;
    if (isEnabled(place.getProject()) && (!(processor instanceof MethodResolverProcessor) || !((MethodResolverProcessor) processor).isConstructor())) {
      final @Nullable NameHint nameHint = processor.getHint(NameHint.KEY);
      final @Nullable String name = nameHint == null ? null : nameHint.getName(state);
      final ElementClassHint classHint = processor.getHint(ElementClassHint.KEY);
      if (classHint == null || classHint.shouldProcess(ElementClassHint.DeclarationKind.METHOD)) {
        @Nullable PsiClass context = PsiTreeUtil.getParentOfType(place, PsiClass.class);
        while (context != null) {
          final @Nullable PsiAnnotation annotation = context.getAnnotation(LombokClassNames.EXTENSION_METHOD);
          if (annotation != null) {
            final Set<PsiClass> providers = PsiAnnotationUtil.getAnnotationValues(annotation, "value", PsiType.class).stream()
              .filter(PsiClassType.class::isInstance)
              .map(PsiClassType.class::cast)
              .map(PsiClassType::resolve)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
            if (!providers.isEmpty()) {
              // Skip existing method signatures of that type.
              final Collection<PsiMethod> collect = supers(psiClass).map(PsiClassUtil::collectClassMethodsIntern).flatMap(Collection::stream).collect(Collectors.toSet());
              // Elimination of duplicate signature methods.
              // The purpose of differentiating providers is to report errors when multiple providers provide the same signature.
              final Map<PsiClass, Collection<PsiMethod>> providerRecord = new HashMap<>();
              PsiType type = null;
              // Try to determine the caller type by context.
              // The reason for getting caller types is the need to distinguish between array types or to infer generics.
              // There's scope for improving the extrapolation process here.
              // Currently the type of reference can only be inferred from the full reference name.
              if (place instanceof PsiMethodCallExpression) {
                final @Nullable PsiExpression expression = ((PsiMethodCallExpression) place).getMethodExpression().getQualifierExpression();
                if (expression != null)
                  type = expression.getType();
              } else if (place instanceof PsiReferenceExpression && ((PsiReferenceExpression) place).getQualifier() != null)
                type = ((PsiReferenceExpression) place).getQualifierExpression().getType();
              // If the context type cannot be inferred, it is inferred by the given PsiClass.
              if (type == null)
                type = PsiTypesUtil.getClassType(psiClass);
              final PsiType finalType = type;
              // Used to apply the caller's generic infers to its parent class or interface.
              PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
              if (finalType instanceof PsiClassType) {
                substitutor = ((PsiClassType) finalType).resolveGenerics().getSubstitutor();
              } else {
                final PsiType deepComponentType = finalType.getDeepComponentType();
                if (deepComponentType instanceof PsiClassType)
                  substitutor = ((PsiClassType) deepComponentType).resolveGenerics().getSubstitutor();
              }
              final PsiSubstitutor finalSubstitutor = substitutor;
              final Stream<PsiMethod> stream = supers(psiClass).flatMap(node -> injectAugment(providers, psiClass == node ? finalType :
                new PsiImmediateClassType(node, TypeConversionUtil.getSuperClassSubstitutor(node, psiClass, finalSubstitutor)), node));
              final boolean match = (name == null ? stream : stream.filter(method -> name.equals(method.getName())))
                .filter(method -> checkMethod(collect, providerRecord, method))
                .allMatch(method -> processor.execute(method, state));
              if (!match)
                return false;
            }
          }
          // Handling annotations from outer classes.
          // e.g. @ExtensionMethod(...) class A { @ExtensionMethod(...) class B { ... } }
          context = PsiTreeUtil.getParentOfType(context, PsiClass.class);
        }
      }
    }
    return true;
  }

  // Traverse the provider to get the method to inject.
  private static Stream<PsiMethod> injectAugment(final Set<PsiClass> providers, final PsiType type, final PsiClass node) {
    return providers.stream()
      .map(ExtensionMethodHandler::providerData)
      .flatMap(Collection::stream)
      .filter(pair -> pair.getFirst().test(type))
      .map(pair -> pair.getSecond().apply(node, type));
  }

  public static List<Pair<Predicate<PsiType>, BiFunction<PsiClass, PsiType, PsiMethod>>> providerData(final PsiClass node) {
    return CachedValuesManager.getCachedValue(node, () -> CachedValueProvider.Result.create(syncProviderData(node), node));
  }

  private static final MapMaker maker = new MapMaker().weakKeys();

  public static class TypeParameterSearcher extends PsiTypeVisitor<Set<PsiTypeParameter>> {

    private final Set<PsiClassType> visited = new HashSet<>();
    private final Set<PsiTypeParameter> result = new HashSet<>();

    @Override
    public Set<PsiTypeParameter> visitType(final PsiType type) { return result; }

    @Override
    public Set<PsiTypeParameter> visitArrayType(final PsiArrayType arrayType) { return arrayType.getComponentType().accept(this); }

    @Override
    public Set<PsiTypeParameter> visitClassType(final PsiClassType classType) {
      if (visited.add(classType)) {
        final PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
        final PsiClass element = resolveResult.getElement();
        if (element instanceof PsiTypeParameter) {
          result.add((PsiTypeParameter) element);
          Stream.of(element.getExtendsListTypes()).forEach(type -> type.accept(this));
        }
        if (element != null) {
          final PsiSubstitutor substitutor = resolveResult.getSubstitutor();
          for (final PsiTypeParameter parameter : PsiUtil.typeParametersIterable(element)) {
            PsiType psiType = substitutor.substitute(parameter);
            if (psiType != null)
              psiType.accept(this);
          }
        }
      }
      return result;
    }

    @Override
    public Set<PsiTypeParameter> visitWildcardType(final PsiWildcardType wildcardType) {
      final PsiType bound = wildcardType.getBound();
      if (bound != null)
        bound.accept(this);
      return result;
    }
  }

  private static List<Pair<Predicate<PsiType>, BiFunction<PsiClass, PsiType, PsiMethod>>> syncProviderData(final PsiClass node) {
    // Improve code completion speed with lazy loading and built-in caching.
    final List<Pair<Predicate<PsiType>, BiFunction<PsiClass, PsiType, PsiMethod>>> result = new ArrayList<>();
    final Project project = node.getProject();
    final PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(project).getResolveHelper();
    final LanguageLevel languageLevel = PsiUtil.getLanguageLevel(project);
    PsiClassUtil.collectClassStaticMethodsIntern(node).stream()
      // There's no judging by scope here, and I don't know if that's any different from the way lombok works.
      .filter(methodNode -> methodNode.hasModifierProperty(PsiModifier.PUBLIC))
      .filter(methodNode -> methodNode.getParameterList().getParametersCount() > 0)
      // Determine if the method works by getting the parameter type. The method may be incomplete, e.g., under preparation.
      .filter(methodNode -> Stream.of(methodNode.getParameterList().getParameters()).map(PsiParameter::getType).allMatch(Objects::nonNull))
      // Cannot be applied to the primitive type.
      .filter(methodNode -> !(methodNode.getParameterList().getParameters()[0].getType() instanceof PsiPrimitiveType))
      .forEach(methodNode -> {
        final PsiType type = methodNode.getParameterList().getParameters()[0].getType();
        final PsiTypeParameter typeParameters[] = methodNode.getTypeParameters();
        final PsiType leftTypes[] = { type };
        final BiFunction<PsiClass, PsiType, PsiMethod> function = (injectNode, injectType) -> {
          // Type parameters that are successfully inferred from the first parameter need to be discarded, as the first parameter is eliminated so that no type constraints can be imposed on the caller.
          // Since this leads to potential type safety issues, the type corresponding to these type parameters needs to be replaced with the inferred type.
          final Set<PsiTypeParameter> dropTypeParameters = type.accept(new TypeParameterSearcher());
          // Type parameters are inferred from the actual types(rightTypes) and the original types(leftTypes).
          final PsiSubstitutor substitutor = resolveHelper.inferTypeArguments(dropTypeParameters.toArray(PsiTypeParameter[]::new), leftTypes, new PsiType[]{ injectType }, languageLevel);
          final LombokLightMethodBuilder lightMethod = new LombokLightMethodBuilder(methodNode.getManager(), methodNode.getName()) {
            @Override
            // This override is used for source methods to be able to find expressions that are referenced by extended methods.
            public boolean isEquivalentTo(final PsiElement another) { return methodNode.isEquivalentTo(another); }
          };
          lightMethod
            .addModifiers(PsiModifier.PUBLIC)
            .setMethodReturnType(substitutor.substitute(methodNode.getReturnType()));
          Stream.of(methodNode.getParameterList().getParameters())
            .skip(1L)
            .map(parameter -> new LombokLightParameter(parameter.getName(), substitutor.substitute(parameter.getType()), lightMethod, JavaLanguage.INSTANCE) {
              @Override
              public boolean equals(final Object o) { // Using the original method will result in an exception when the stub changes.
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                final LombokLightParameter parameter = (LombokLightParameter) o;
                return Objects.equals(getName(), parameter.getName()) && Objects.equals(getType(), parameter.getType());
              }
            })
            .forEach(lightMethod::addParameter);
          Stream.of(methodNode.getThrowsList().getReferencedTypes())
            .map(substitutor::substitute)
            .map(PsiClassType.class::cast)
            .forEach(lightMethod::addException);
          Stream.of(typeParameters)
            .filter(targetTypeParameter -> !dropTypeParameters.contains(targetTypeParameter))
            .forEach(lightMethod::addTypeParameter);
          lightMethod.setNavigationElement(methodNode);
          lightMethod.setContainingClass(injectNode);
          if (injectNode.isInterface())
            lightMethod.addModifier(PsiModifier.DEFAULT);
          return lightMethod;
        };
        // Use weak reference maps for caching to avoid potential memory leaks.
        final Map<PsiType, Map<PsiClass, PsiMethod>> cache = maker.makeMap();
        result.add(Pair.create(
          injectType -> TypeConversionUtil.isAssignable(resolveHelper.inferTypeArguments(typeParameters, leftTypes, new PsiType[]{ injectType }, languageLevel).substitute(type), injectType),
          (injectNode, injectType) -> cache.computeIfAbsent(injectType, $ -> maker.makeMap()).computeIfAbsent(injectNode, $ -> function.apply(injectNode, injectType))));
      });
    return result;
  }

  @NotNull
  private static Stream<PsiClass> supers(PsiClass psiClass) { return Stream.concat(Stream.of(psiClass), InheritanceUtil.getSuperClasses(psiClass).stream()); }

  private static boolean checkMethod(final Collection<PsiMethod> members, final Map<PsiClass, Collection<PsiMethod>> record, final PsiMethod methodTree) {
    final Collection<PsiMethod> collection = record.computeIfAbsent(((PsiMethod) methodTree.getNavigationElement()).getContainingClass(), $ -> new ArrayList<>());
    try {
      return Stream.concat(members.stream(), collection.stream())
        .map(tree -> MethodSignatureBackedByPsiMethod.create(tree, PsiSubstitutor.EMPTY, true))
        .noneMatch(MethodSignatureBackedByPsiMethod.create(methodTree, PsiSubstitutor.EMPTY, true)::equals);
    } finally { collection.add(methodTree); }
  }

}
