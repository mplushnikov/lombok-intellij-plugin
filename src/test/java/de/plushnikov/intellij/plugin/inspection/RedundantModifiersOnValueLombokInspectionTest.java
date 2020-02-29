package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;

public class RedundantModifiersOnValueLombokInspectionTest extends LombokInspectionTest {

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/redundantModifierInspection";
  }

  @Override
  protected InspectionProfileEntry getInspection() {
    return new RedundantModifiersOnValueLombokAnnotationInspection();
  }

  public void testValueClass() {
    doTest();
  }

}
