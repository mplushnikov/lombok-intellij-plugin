package de.plushnikov.intellij.plugin.hack.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiMethodReferenceExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.light.LightParameter;
import com.intellij.psi.impl.light.LightTypeParameterListBuilder;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.processor.MethodResolverProcessor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.TypeConversionUtil;

import com.google.common.collect.MapMaker;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EMHandler {

  public static boolean processDeclarations(final boolean result, final PsiClass psiClass, final PsiScopeProcessor processor, final ResolveState state, final PsiElement lastParent, final PsiElement place) {
    if (!result)
      return false;
    if (place instanceof PsiMethodReferenceExpression)
      return result;
    if ((!(processor instanceof MethodResolverProcessor) || !((MethodResolverProcessor) processor).isConstructor())) {
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
            final @Nullable NameHint nameHint = processor.getHint(NameHint.KEY);
            final @Nullable String name = nameHint == null ? null : nameHint.getName(state);
            final ElementClassHint classHint = processor.getHint(ElementClassHint.KEY);
            if (classHint == null || classHint.shouldProcess(ElementClassHint.DeclarationKind.METHOD)) {
              final List<PsiElement> collect = supers(psiClass).map(PsiClassUtil::collectClassMethodsIntern).flatMap(Collection::stream).collect(Collectors.toList());
              PsiType type = null;
              if (place instanceof PsiMethodCallExpression) {
                final @Nullable PsiExpression expression = ((PsiMethodCallExpression) place).getMethodExpression().getQualifierExpression();
                if (expression != null)
                  type = expression.getType();
              } else if (place instanceof PsiReferenceExpression && ((PsiReferenceExpression) place).getQualifier() != null)
                type = ((PsiReferenceExpression) place).getQualifierExpression().getType();
              if (type == null)
                type = PsiTypesUtil.getClassType(psiClass);
              final boolean match = injectAugment(providers, type, psiClass)
                .filter(method -> name == null || method.getName().equals(name))
                .filter(method -> checkMethod(collect, method))
                .allMatch(method -> processor.execute(method, state));
              if (!match)
                return false;
            }
          }
        }
        context = PsiTreeUtil.getParentOfType(context, PsiClass.class);
      }
    }
    return true;
  }

  private static Stream<PsiMethod> injectAugment(final Set<PsiClass> providers, final PsiType type, final PsiClass node) {
    return providers.stream().map(EMHandler::providerData)
      .map(Map::entrySet)
      .flatMap(Set::stream)
      .filter(e -> checkType(e.getKey(), type, node))
      .map(Map.Entry::getValue)
      .flatMap(List::stream)
      .map(function -> {
        try {
          return function.apply(node);
        } catch (Throwable throwable) {
          if (throwable instanceof ProcessCanceledException)
            throw throwable;
          throwable.printStackTrace();
          return null;
        }
      })
      .filter(Objects::nonNull);
  }

  public static boolean checkType(final PsiType type, final PsiType nodeType, final PsiClass node) {
    if (type == null)
      return false;
    if (type.equals(nodeType))
      return true;
    if (type instanceof PsiClassType) {
      final PsiClass resolved = ((PsiClassType) type).resolve();
      if (resolved instanceof PsiTypeParameter) {
        for (final PsiClassType bound : resolved.getExtendsListTypes())
          if (!TypeConversionUtil.isAssignable(bound, nodeType))
            return false;
        return true;
      } else
        return node.equals(resolved);
    }
    return TypeConversionUtil.isAssignable(type, nodeType);
  }

  public static Map<PsiType, List<Function<PsiClass, PsiMethod>>> providerData(final PsiClass node) {
    return CachedValuesManager.getCachedValue(node, () -> CachedValueProvider.Result.create(syncProviderData(node), node, PsiModificationTracker.MODIFICATION_COUNT));
  }

  private static Map<PsiType, List<Function<PsiClass, PsiMethod>>> syncProviderData(final PsiClass node) {
    final Map<PsiType, List<Function<PsiClass, PsiMethod>>> result = new ConcurrentHashMap<>();
    Stream.of(node.getMethods())
      .filter(methodNode -> methodNode.hasModifierProperty(PsiModifier.STATIC) && methodNode.hasModifierProperty(PsiModifier.PUBLIC))
      .filter(methodNode -> methodNode.getParameterList().getParametersCount() > 0)
      .filter(methodNode -> !(methodNode.getParameterList().getParameters()[0].getType() instanceof PsiPrimitiveType))
      .forEach(methodNode -> {
        final PsiType type = methodNode.getParameterList().getParameters()[0].getType();
        final Function<PsiClass, PsiMethod> function = injectNode -> {
          final List<PsiTypeParameter> dropTypeParameters = new LinkedList<>();
          final Function<PsiType, PsiType> typeMapper;
          if (type instanceof PsiClassType) {
            final Map<PsiType, PsiType> mapping = new HashMap<>();
            final PsiClass resolve = ((PsiClassType) type).resolve();
            if (resolve instanceof PsiTypeParameter)
              mapping.put(type, "_Dummy_.__Array__".equals(injectNode.getQualifiedName()) ?
                PsiElementFactory.getInstance(node.getProject()).createTypeByFQClassName(Object.class.getCanonicalName()).createArrayType() :
                new PsiImmediateClassType(injectNode, PsiSubstitutor.EMPTY));
            final PsiType[] superTypes = type.getSuperTypes();
            final PsiType parameters[] = superTypes.length > 0 ? superTypes[0] instanceof PsiClassType ? ((PsiClassType) superTypes[0]).getParameters()
              : ((PsiClassType) type).getParameters() : ((PsiClassType) type).getParameters();
            if (parameters.length > 0) {
              final int p_count[] = { -1 };
              final PsiTypeParameter resolveTypeParameters[] = injectNode.getTypeParameters();
              Stream.of(parameters)
                .peek($ -> p_count[0]++)
                .filter(PsiClassType.class::isInstance)
                .map(PsiClassType.class::cast)
                .forEach(classType -> {
                  final PsiClass resolveParameter = classType.resolve();
                  if (resolveParameter instanceof PsiTypeParameter) {
                    final PsiTypeParameter parameter = (PsiTypeParameter) resolveParameter;
                    dropTypeParameters.add(parameter);
                    mapping.put(classType, new PsiImmediateClassType(resolveTypeParameters[p_count[0]], PsiSubstitutor.EMPTY));
                  }
                });
            }
            if (mapping.isEmpty())
              typeMapper = Function.identity();
            else {
              final TypeMapper mapper = new TypeMapper(mapping);
              typeMapper = targetType -> targetType.accept(mapper);
            }
          } else
            typeMapper = Function.identity();
          final LombokLightMethodBuilder lightMethod = new LombokLightMethodBuilder(methodNode.getManager(), methodNode.getName());
          lightMethod
            .addModifiers(PsiModifier.PUBLIC)
            .setMethodReturnType(typeMapper.apply(methodNode.getReturnType()));
          Stream.of(methodNode.getParameterList().getParameters())
            .skip(1L)
            .map(parameter -> new LightParameter(parameter.getName(), typeMapper.apply(parameter.getType()), lightMethod, JavaLanguage.INSTANCE, parameter.isVarArgs()))
            .forEach(lightMethod::addParameter);
          Stream.of(methodNode.getThrowsList().getReferencedTypes())
            .map(typeMapper)
            .map(PsiClassType.class::cast)
            .forEach(lightMethod::addException);
          Stream.of(methodNode.getTypeParameters())
            .filter(targetTypeParameter -> !dropTypeParameters.contains(targetTypeParameter))
            .forEach(((LightTypeParameterListBuilder) Objects.requireNonNull(lightMethod.getTypeParameterList()))::addParameter);
          lightMethod.setNavigationElement(methodNode);
          lightMethod.setContainingClass(injectNode);
          if (injectNode.isInterface())
            lightMethod.addModifier(PsiModifier.DEFAULT);
          return lightMethod;
        };
        final Map<PsiClass, PsiMethod> cache = new MapMaker().weakKeys().makeMap();
        result.computeIfAbsent(type, key -> new ArrayList<>()).add(containingClass -> cache.computeIfAbsent(containingClass, function));
      });
    return result;
  }

  @NotNull
  private static Stream<PsiClass> supers(PsiClass psiClass) {
    return Stream.concat(Stream.of(psiClass), Stream.of(PsiClassImplUtil.getSupers(psiClass)));
  }

  private static boolean checkMethod(final List<PsiElement> members, final PsiMethod methodTree) {
    return members.stream()
      .filter(PsiMethod.class::isInstance)
      .map(PsiMethod.class::cast)
      .map(tree -> MethodSignatureBackedByPsiMethod.create(tree, PsiSubstitutor.EMPTY, true))
      .noneMatch(MethodSignatureBackedByPsiMethod.create(methodTree, PsiSubstitutor.EMPTY, true)::equals);
  }

}
