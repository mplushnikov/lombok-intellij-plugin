package de.plushnikov.intellij.plugin.highlights;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.testFramework.LightProjectDescriptor;
import com.siyeh.ig.LightInspectionTestCase;
import de.plushnikov.intellij.plugin.LombokTestUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.testFramework.LightPlatformTestCase.getModule;


/**
 * @author Lekanich
 */
public abstract class AbstractLombokHighlightsTest extends LightInspectionTestCase {
  public static final String TEST_DATA_INSPECTION_DIRECTORY = "testData/highlights";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    LombokTestUtil.loadLombokLibrary(myFixture.getProjectDisposable(), getModule());
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return LombokTestUtil.getProjectDescriptor();
  }

  @Override
  protected InspectionProfileEntry getInspection() {
    return null;
  }
}

