package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.defUse.DefUseInspection;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.BuildNumber;
import org.jetbrains.annotations.Nullable;


public class DefUseInspectionTest extends LombokInspectionTest {
  @Override
  protected String getTestDataPath() {
    return TEST_DATA_INSPECTION_DIRECTORY + "/diverse";
  }

  @Nullable
  @Override
  protected InspectionProfileEntry getInspection() {
    return new DefUseInspection();
  }

  public void testIssue633() {
    final BuildNumber buildNumber = ApplicationInfo.getInstance().getBuild();
    if (173 <= buildNumber.getBaselineVersion()) {
      doTest();
    }
  }
}
