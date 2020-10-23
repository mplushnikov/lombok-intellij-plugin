package de.plushnikov.intellij.plugin.hack.extension;

import java.util.List;
import java.util.Map;

import com.intellij.psi.Bottom;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiCapturedWildcardType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDiamondType;
import com.intellij.psi.PsiDisjunctionType;
import com.intellij.psi.PsiEllipsisType;
import com.intellij.psi.PsiIntersectionType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeVariable;
import com.intellij.psi.PsiTypeVisitorEx;
import com.intellij.psi.PsiWildcardType;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import com.intellij.util.SmartList;

import org.jetbrains.annotations.Nullable;

public class TypeMapper extends PsiTypeVisitorEx<PsiType> {

  public final Map<PsiType, PsiType> mapping;

  public TypeMapper(final Map<PsiType, PsiType> mapping) { this.mapping = mapping; }

  public PsiType mapType(final PsiType type) { return type.accept(this); }

  @Override
  public PsiType visitArrayType(final PsiArrayType type) {
    final PsiType componentType = type.getComponentType();
    final PsiType mappedComponent = mapType(componentType);
    if (mappedComponent == componentType)
      return type;
    return new PsiArrayType(mappedComponent, type.getAnnotationProvider());
  }

  @Override
  public PsiType visitEllipsisType(final PsiEllipsisType type) {
    final PsiType componentType = type.getComponentType();
    final PsiType mappedComponent = mapType(componentType);
    if (mappedComponent == componentType)
      return type;
    return new PsiEllipsisType(mappedComponent, type.getAnnotationProvider());
  }

  @Override
  public PsiType visitTypeVariable(final PsiTypeVariable var) { return var; }

  @Override
  public PsiType visitBottom(final Bottom bottom) { return bottom; }

  @Override
  public PsiType visitCapturedWildcardType(final PsiCapturedWildcardType type) { return type; }

  @Override
  public PsiType visitPrimitiveType(final PsiPrimitiveType primitiveType) { return primitiveType; }

  @Override
  public PsiType visitType(final PsiType type) { return type; }

  @Override
  public PsiType visitWildcardType(final PsiWildcardType wildcardType) {
    final @Nullable PsiType bound = wildcardType.getBound();
    final PsiManager manager = wildcardType.getManager();
    if (bound == null)
      return PsiWildcardType.createUnbounded(manager);
    final PsiType newBound = mapType(bound);
    return newBound == bound ? wildcardType : wildcardType.isExtends() ? PsiWildcardType.createExtends(manager, newBound) : PsiWildcardType.createSuper(manager, newBound);
  }

  @Override
  public PsiType visitIntersectionType(final PsiIntersectionType intersectionType) {
    final List<PsiType> substituted = new SmartList<>();
    boolean flag = false;
    for (final PsiType component : intersectionType.getConjuncts()) {
      final PsiType mapped = mapType(component);
      flag |= mapped != component;
      substituted.add(mapped);
    }
    return flag ? PsiIntersectionType.createIntersection(false, substituted.toArray(PsiType.EMPTY_ARRAY)) : intersectionType;
  }

  @Override
  public PsiType visitDisjunctionType(final PsiDisjunctionType disjunctionType) {
    final List<PsiType> substituted = new SmartList<>();
    boolean flag = false;
    for (final PsiType component : disjunctionType.getDisjunctions()) {
      final PsiType mapped = mapType(component);
      flag |= mapped != component;
      substituted.add(mapped);
    }
    return flag ? disjunctionType.newDisjunctionType(substituted) : disjunctionType;
  }

  @Override
  public PsiType visitDiamondType(final PsiDiamondType diamondType) { return diamondType; }

  @Override
  public PsiType visitClassType(final PsiClassType type) {
    final PsiType result = mapping.getOrDefault(type, type);
    if (result != type || ((PsiClassType) result).getParameters().length == 0)
      return result;
    final PsiClassType.ClassResolveResult classResolveResult = type.resolveGenerics();
    final PsiClass psiClass = classResolveResult.getElement();
    if (psiClass == null)
      return type;
    PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
    for (final Map.Entry<PsiTypeParameter, PsiType> entry : classResolveResult.getSubstitutor().getSubstitutionMap().entrySet()) {
      final PsiType value = entry.getValue();
      substitutor = substitutor.put(entry.getKey(), value == null ? null : mapType(value));
    }
    return new PsiImmediateClassType(psiClass, substitutor);
  }

}
