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
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiEllipsisType;
import com.intellij.psi.PsiIntersectionType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVariable;
import com.intellij.psi.PsiTypeVisitorEx;
import com.intellij.psi.PsiWildcardType;
import com.intellij.util.SmartList;

import org.jetbrains.annotations.Nullable;

public class TypeMapper extends PsiTypeVisitorEx<PsiType> {

  public final Map<PsiType, PsiType> mapping;

  public PsiType mapType(final PsiType type) {
    final @Nullable PsiType result = mapping.get(type);
    return result == null ? type instanceof PsiClassType ? type : type.accept(this) : result;
  }

  @Override
  public PsiType visitArrayType(final PsiArrayType type) {
    PsiType componentType = type.getComponentType();
    PsiType mappedComponent = mapType(componentType);
    if (mappedComponent == null)
      return null;
    if (mappedComponent == componentType)
      return type;
    return new PsiArrayType(mappedComponent, type.getAnnotationProvider());
  }

  @Override
  public PsiType visitEllipsisType(final PsiEllipsisType type) {
    PsiType componentType = type.getComponentType();
    PsiType mappedComponent = mapType(componentType);
    if (mappedComponent == null)
      return null;
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
    PsiType bound = wildcardType.getBound();
    final PsiManager manager = wildcardType.getManager();
    if (bound == null)
      return PsiWildcardType.createUnbounded(manager);
    bound = mapType(bound);
    if (bound == null) return null;
    return wildcardType.isExtends() ? PsiWildcardType.createExtends(manager, bound) : PsiWildcardType.createSuper(manager, bound);
  }

  @Override
  public PsiType visitIntersectionType(final PsiIntersectionType intersectionType) {
    final List<PsiType> substituted = new SmartList<>();
    for (PsiType component : intersectionType.getConjuncts()) {
      PsiType mapped = mapType(component);
      if (mapped == null)
        return null;
      substituted.add(mapped);
    }
    return PsiIntersectionType.createIntersection(false, substituted.toArray(PsiType.EMPTY_ARRAY));
  }

  @Override
  public PsiType visitDisjunctionType(PsiDisjunctionType disjunctionType) {
    final List<PsiType> substituted = new SmartList<>();
    for (PsiType component : disjunctionType.getDisjunctions()) {
      PsiType mapped = mapType(component);
      if (mapped == null)
        return null;
      substituted.add(mapped);
    }
    return disjunctionType.newDisjunctionType(substituted);
  }

  @Override
  public PsiType visitDiamondType(final PsiDiamondType diamondType) { return diamondType; }

  @Override
  public PsiType visitClassType(final PsiClassType type) {
    final PsiType result = mapType(type);
    if (result != type || !(result instanceof PsiClassType) || ((PsiClassType) result).getParameters().length == 0)
      return result;
    final PsiClass resolve = type.resolve();
    if (resolve == null)
      return type;
    final PsiType typeParameters[] = type.getParameters();
    for (int i = 0; i < typeParameters.length; i++)
      typeParameters[i] = typeParameters[i].accept(this);
    return PsiElementFactory.getInstance(resolve.getProject()).createType(resolve, typeParameters);
  }

  public TypeMapper(final Map<PsiType, PsiType> mapping) { this.mapping = mapping; }

  public static PsiType map(final Map<PsiType, PsiType> mapping, final PsiType type) { return type.accept(new TypeMapper(mapping)); }

}
