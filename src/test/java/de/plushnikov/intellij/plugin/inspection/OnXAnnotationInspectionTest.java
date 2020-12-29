package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import de.plushnikov.intellij.plugin.LombokTestUtil;

public class OnXAnnotationInspectionTest extends LombokInspectionTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    LombokTestUtil.loadJsr305Library(myFixture.getProjectDisposable(), getModule());
  }

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/onXAnnotation";
  }

  @Override
  protected InspectionProfileEntry getInspection() {
    return new LombokInspection();
  }

  public void testConstructorOnConstructor() {
    doTest();
  }

  public void testGetterOnMethod() {
    doTest();
  }

  public void testSetterOnParam() {
    doTest();
  }

}
