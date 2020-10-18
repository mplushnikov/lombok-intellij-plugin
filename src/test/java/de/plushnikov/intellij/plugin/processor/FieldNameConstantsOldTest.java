package de.plushnikov.intellij.plugin.processor;

import com.intellij.openapi.util.RecursionManager;
import de.plushnikov.intellij.plugin.AbstractLombokParsingTestCase;
import de.plushnikov.intellij.plugin.LombokTestUtil;

/**
 * Unit tests for @FieldNameConstants annotation from old version of lombok
 */
public class FieldNameConstantsOldTest extends AbstractLombokParsingTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();

    //TODO disable assertions for the moment
    RecursionManager.disableMissedCacheAssertions(myFixture.getProjectDisposable());
  }

  public void testFieldnameconstants$FieldNameConstantsOldBasic() {
    doTest(true);
  }

  @Override
  protected void loadLombokLibrary() {
    LombokTestUtil.loadOldLombokLibrary(myFixture.getProjectDisposable(), getModule());
  }
}
