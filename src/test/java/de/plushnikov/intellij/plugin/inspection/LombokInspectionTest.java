package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.LightProjectDescriptor;
import com.siyeh.ig.LightInspectionTestCase;
import de.plushnikov.intellij.plugin.LombokTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.intellij.testFramework.LightPlatformTestCase.getModule;

public abstract class LombokInspectionTest extends LightInspectionTestCase {
  static final String TEST_DATA_INSPECTION_DIRECTORY = "testData/inspection";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    LombokTestUtil.loadLombokLibrary(myFixture, getModule());
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return LombokTestUtil.getProjectDescriptor();
  }

  protected void doCheckQuickFix(String message) {
    final List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes();
    final Optional<String> quickFixWithMessage = allQuickFixes.stream()
      .map(IntentionAction::getText)
      .filter(message::equals)
      .findAny();
    assertTrue(quickFixWithMessage.isPresent());
  }
}
