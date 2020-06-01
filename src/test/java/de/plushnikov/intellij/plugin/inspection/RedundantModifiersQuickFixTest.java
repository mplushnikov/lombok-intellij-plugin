package de.plushnikov.intellij.plugin.inspection;

import de.plushnikov.intellij.plugin.AbstractLombokLightCodeInsightTestCase;
import de.plushnikov.intellij.plugin.inspection.modifiers.RedundantModifiersOnUtilityClassLombokAnnotationInspection;
import de.plushnikov.intellij.plugin.inspection.modifiers.RedundantModifiersOnValueLombokAnnotationInspection;

import static de.plushnikov.intellij.plugin.inspection.LombokInspectionTest.TEST_DATA_INSPECTION_DIRECTORY;

public class RedundantModifiersQuickFixTest extends AbstractLombokLightCodeInsightTestCase {

  @Override
  protected String getBasePath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/redundantModifierInspection";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new RedundantModifiersOnValueLombokAnnotationInspection(),
      new RedundantModifiersOnUtilityClassLombokAnnotationInspection());
  }

  public void testUtilityClassClassWithStaticField() {
    checkQuickFix("@UtilityClass already marks fields static.");
  }

  public void testUtilityClassClassWithStaticMethod() {
    checkQuickFix("@UtilityClass already marks methods static.");
  }

  public void testUtilityClassClassWithStaticInnerClass() {
    checkQuickFix("@UtilityClass already marks inner classes static.");
  }

  public void testValueClassWithPrivateField() {
    checkQuickFix("@Value already marks non-static, package-local fields private.");
  }

  public void testValueClassWithFinalField() {
    checkQuickFix("@Value already marks non-static fields final.");
  }
}
