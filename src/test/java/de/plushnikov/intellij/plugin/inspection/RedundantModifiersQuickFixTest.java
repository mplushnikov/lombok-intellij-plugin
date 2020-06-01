package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInsight.intention.IntentionAction;
import de.plushnikov.intellij.plugin.AbstractLombokLightCodeInsightTestCase;

import java.util.List;
import java.util.Optional;

import static de.plushnikov.intellij.plugin.inspection.LombokInspectionTest.TEST_DATA_INSPECTION_DIRECTORY;

public class RedundantModifiersQuickFixTest extends AbstractLombokLightCodeInsightTestCase {

  @Override
  protected String getBasePath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/redundantModifierInspection";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new RedundantModifiersOnValueLombokAnnotationInspection());
  }

  public void testValueClassWithPrivateField() {
    final List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes(getBasePath() + '/' + getTestName(false) + ".java");
    final Optional<String> removeModifierFix = allQuickFixes.stream().map(IntentionAction::getText)
      .filter("Remove 'private' modifier"::equals).findFirst();

    assertTrue("Redundant private field modifier QuickFix not found", removeModifierFix.isPresent());
  }

  public void testValueClassWithFinalField() {
    final List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes(getBasePath() + '/' + getTestName(false) + ".java");
    final Optional<String> removeModifierFix = allQuickFixes.stream().map(IntentionAction::getText)
      .filter("Remove 'final' modifier"::equals).findFirst();

    assertTrue("Redundant final field modifier QuickFix not found", removeModifierFix.isPresent());
  }

  public void testUtilityClassClassWithStaticField() {
    findAccessModifierActions("@UtilityClass already marks fields static.");
  }

  public void testUtilityClassClassWithStaticMethod() {
    findAccessModifierActions("@UtilityClass already marks methods static.");
  }

  public void testUtilityClassClassWithStaticInnerClass() {
    findAccessModifierActions("@UtilityClass already marks inner classes static.");
  }

  public void testValueClassWithPrivateField() {
    findAccessModifierActions("@Value already marks non-static, package-local fields private.");
  }

  public void testValueClassWithFinalField() {
    findAccessModifierActions("@Value already marks non-static fields final.");
  }

  private void findAccessModifierActions(String message) {
    myFixture.configureByFile(getBasePath() + '/' + getTestName(false) + ".java");

    final List<IntentionAction> availableActions = getAvailableActions();
    assertTrue(message,
      availableActions.stream().anyMatch(action -> action.getText().contains("Change access modifier")));
  }

    assertTrue("Redundant final field modifier QuickFix not found", removeModifierFix.isPresent());
  }
}
