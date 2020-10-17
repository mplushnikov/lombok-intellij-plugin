package de.plushnikov.intellij.plugin.intention;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
public class ReplaceFromFieldWithLombokAnnotationActionTest extends LombokIntentionActionTest {
  private Collection<Class<? extends Annotation>> expectedAnnotations = Collections.emptyList();

  @Override
  protected String getBasePath() {
    return TEST_DATA_INTENTION_DIRECTORY + "/replaceLombok";
  }

  @Override
  public IntentionAction getIntentionAction() {
    return new ReplaceWithLombokAnnotationAction();
  }

  @Override
  public boolean wasInvocationSuccessful() {
    PsiElement elementAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    PsiField field = Optional.ofNullable(PsiTreeUtil.getParentOfType(elementAtCaret, PsiField.class))
      .orElseGet(() -> PsiTreeUtil.findChildOfType(myFixture.getFile(), PsiField.class));
    if (field == null) {
      return false;
    }

    Set<String> requiredAnnotations = expectedAnnotations.stream()
      .map(Class::getCanonicalName)
      .collect(Collectors.toSet());

    @NotNull PsiAnnotation[] annotations = Optional.ofNullable(field)
      .map(PsiField::getModifierList)
      .map(PsiModifierList::getAnnotations)
      .orElse(new PsiAnnotation[0]);
    return Stream.of(annotations)
      .filter(annotation -> requiredAnnotations.contains(annotation.getQualifiedName()))
      .distinct()
      .count() == expectedAnnotations.size() && expectedAnnotations.size() == annotations.length;
  }

  public void testReplaceGetterFromField() {
    setExpectedAnnotations(Getter.class);
    doTest();
  }

  public void testReplaceSetterFromField() {
    setExpectedAnnotations(Setter.class);
    doTest();
  }

  public void testReplaceAccessorsFromField() {
    setExpectedAnnotations(Getter.class, Setter.class);
    doTest();
  }

  public void testNotReplaceIncorrectAccessors() {
    setExpectedAnnotations();
    doTest();
  }

  public void testReplaceGetterFromMethod() {
    setExpectedAnnotations(Getter.class);
    doTest();
  }

  public void testReplaceSetterFromMethod() {
    setExpectedAnnotations(Setter.class);
    doTest();
  }

  public void testReplaceGetterFromMethod2() {
    setExpectedAnnotations(Getter.class);
    doTest();
  }

  public void testReplaceSetterFromMethod2() {
    setExpectedAnnotations(Setter.class);
    doTest();
  }

  public void testReplaceGetterFromFieldNotCompleteMethod() {
    setExpectedAnnotations(Getter.class);
    doTest();
  }

  public void testReplaceSetterFromFieldNotCompleteMethod() {
    setExpectedAnnotations(Setter.class);
    doTest();
  }

  public void testNotReplaceAbstractGetterFromField() {
    setExpectedAnnotations();
    doTest();
  }

  public void testNotReplaceAbstractSetterFromField() {
    setExpectedAnnotations();
    doTest();
  }

  public void testNotReplaceSetterWithWrongParamFromField() {
    setExpectedAnnotations();
    doTest();
  }

  private void setExpectedAnnotations(Class<? extends Annotation>... annotations) {
    this.expectedAnnotations = Arrays.asList(annotations);
  }
}
