package de.plushnikov.intellij.plugin.action;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import de.plushnikov.lombok.LombokLightCodeInsightTestCase;
import junit.framework.ComparisonFailure;

import java.io.File;
import java.io.IOException;

public abstract class LombokLightActionTest extends LombokLightCodeInsightTestCase {
  protected void doTest() throws Exception {
    myFixture.configureByFile(getBasePath() + "/before" + getTestName(false) + ".java");
    performActionTest();
    checkResultByFile(getBasePath() + "/after" + getTestName(false) + ".java");
  }

  private void checkResultByFile(String expectedFile) throws IOException {
    try {
      myFixture.checkResultByFile(expectedFile, true);
    } catch (ComparisonFailure ex) {
      checkResultManually(expectedFile);
    }
  }

  private void checkResultManually(String expectedFile) throws IOException {
    String actualFileText = myFixture.getFile().getText();
    actualFileText = actualFileText.replace("java.lang.", "");

    final String path = "." + "/" + expectedFile;
    String expectedFileText = StringUtil.convertLineSeparators(FileUtil.loadFile(new File(path)));

    assertEquals(expectedFileText.replaceAll("\\s+", ""), actualFileText.replaceAll("\\s+", ""));
  }

  private void performActionTest() {
    AnAction anAction = getAction();
    anAction.actionPerformed(createAnActionEvent(anAction));
    FileDocumentManager.getInstance().saveAllDocuments();
  }

  private AnActionEvent createAnActionEvent(AnAction anAction) {
    return new AnActionEvent(
        null,
        DataManager.getInstance().getDataContext(),
        "",
        anAction.getTemplatePresentation(),
        ActionManager.getInstance(),
        0
    );
  }

  protected abstract AnAction getAction();
}
