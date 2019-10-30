package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.dataFlow.DataFlowInspection;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.BuildNumber;

public class DataFlowInspectionTest extends LombokInspectionTest {

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/diverse";
  }

  @Override
  protected InspectionProfileEntry getInspection() {
    return new DataFlowInspection();
  }

  public void testIssue440() {
    final BuildNumber buildNumber = ApplicationInfo.getInstance().getBuild();
    if (183 <= buildNumber.getBaselineVersion()) {
      doTest();
    } else {
      doNamedTest("Issue440Prior183");
    }
  }

}
