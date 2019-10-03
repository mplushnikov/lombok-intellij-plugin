package de.plushnikov.intellij.plugin.inspection;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.testFramework.LightProjectDescriptor;
import com.siyeh.ig.LightJavaInspectionTestCase;
import de.plushnikov.intellij.plugin.LombokTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.util.PathUtil;
import com.siyeh.ig.LightInspectionTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class LombokInspectionTest extends LightInspectionTestCase {
  static final String TEST_DATA_INSPECTION_DIRECTORY = "testData/inspection";

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
}
