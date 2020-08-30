package de.plushnikov.intellij.plugin;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ex.QuickFixWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import de.plushnikov.intellij.plugin.util.ReflectionUtil;
import junit.framework.ComparisonFailure;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.intellij.testFramework.LightPlatformTestCase.getModule;

public abstract class AbstractLombokLightCodeInsightTestCase extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return ".";
  }

  @Override
  protected String getBasePath() {
    return "testData";
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return LombokTestUtil.getProjectDescriptor();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    loadLombokLibrary();
  }

  protected void loadLombokLibrary() {
    LombokTestUtil.loadLombokLibrary(myFixture, getModule());
  }

  protected PsiFile loadToPsiFile(String fileName) {
    final String sourceFilePath = getBasePath() + "/" + fileName;
    VirtualFile virtualFile = myFixture.copyFileToProject(sourceFilePath, fileName);
    myFixture.configureFromExistingVirtualFile(virtualFile);
    return myFixture.getFile();
  }

  protected void checkResultByFile(String expectedFile) throws IOException {
    try {
      myFixture.checkResultByFile(expectedFile, true);
    } catch (ComparisonFailure ex) {
      String actualFileText = myFixture.getFile().getText();
      actualFileText = actualFileText.replace("java.lang.", "");

      final String path = getTestDataPath() + "/" + expectedFile;
      String expectedFileText = StringUtil.convertLineSeparators(FileUtil.loadFile(new File(path)));

      if (!expectedFileText.replaceAll("\\s+", "").equals(actualFileText.replaceAll("\\s+", ""))) {
        assertEquals(expectedFileText, actualFileText);
      }
    }
  }

  protected void doCheckQuickFix(String message) {
    final List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes(getBasePath() + '/' + getTestName(false) + ".java");
    final Optional<String> quickFixWithMessage = allQuickFixes.stream()
      .filter(QuickFixWrapper.class::isInstance)
      .map(QuickFixWrapper.class::cast)
      .map(this::getQuickFixWrapperDescriptionTemplate)
      .filter(message::equals).findAny();
    assertTrue(quickFixWithMessage.isPresent());
  }

  private String getQuickFixWrapperDescriptionTemplate(QuickFixWrapper quickFixWrapper) {
    ProblemDescriptor myDescriptor = ReflectionUtil.getFinalFieldPerReflection(QuickFixWrapper.class, quickFixWrapper, ProblemDescriptor.class);
    return myDescriptor.getDescriptionTemplate();
  }
}
