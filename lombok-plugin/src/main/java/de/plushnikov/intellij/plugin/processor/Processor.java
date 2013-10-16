package de.plushnikov.intellij.plugin.processor;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
public interface Processor {
  boolean acceptAnnotation(@NotNull PsiAnnotation psiAnnotation, @NotNull Class<? extends PsiElement> type);

  @NotNull
  String getSupportedAnnotation();

  Class<? extends Annotation> getSupportedAnnotationClass();

  Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation);


  boolean isEnabled(@NotNull Project project);

  boolean canProduce(@NotNull Class<? extends PsiElement> type);

  @NotNull
  List<? super PsiElement> process(@NotNull PsiClass psiClass);
}
