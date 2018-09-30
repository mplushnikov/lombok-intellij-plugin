package de.plushnikov.intellij.plugin.usage;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.usageView.UsageInfo;
import de.plushnikov.intellij.plugin.AbstractLombokLightCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Test for lombok find usage extension
 */
public class LombokUsageTest extends AbstractLombokLightCodeInsightTestCase {

  public void testFindUsageGetterSetter() {
    final Collection<UsageInfo> usages = loadTestClass();
    assertUsages(usages, "findUsageGetterSetter.setBar", "findUsageGetterSetter.getBar");
  }

  public void testFindUsageAccessors() {
    final Collection<UsageInfo> usages = loadTestClass();
    assertUsages(usages, "findUsageAccessors.setBar", "findUsageAccessors.getBar");
  }

  public void testFindUsageWither() {
    final Collection<UsageInfo> usages = loadTestClass();
    assertUsages(usages, "findUsageWither.withBar", "findUsageWither.getBar");
  }

  public void testFindUsageBuilder() {
    final Collection<UsageInfo> usages = loadTestClass();
    assertUsages(usages, "FindUsageBuilder.builder().bar", "findUsageBuilder.getBar");
  }

  public void testFindUsageSingularBuilder() {
    final Collection<UsageInfo> usages = loadTestClass();
    assertUsages(usages, "FindUsageSingularBuilder.builder().bar", "FindUsageSingularBuilder.builder().bars",
      "FindUsageSingularBuilder.builder().clearBars", "findUsageBuilder.getBars");
  }

  private void assertUsages(Collection<UsageInfo> usages, String... usageTexts) {
    assertEquals(usageTexts.length, usages.size());
    List<UsageInfo> sortedUsages = new ArrayList<UsageInfo>(usages);
    sortedUsages.sort(LombokUsageTest::compareToByStartOffset);
    for (int i = 0; i < usageTexts.length; i++) {
      assertEquals(usageTexts[i], sortedUsages.get(i).getElement().getText().replaceAll("\\s*", ""));
    }
  }

  /**
   * Copy from IntelliJ sources UsageInfo.compareToByStartOffset
   */
  private static int compareToByStartOffset(@NotNull UsageInfo info0, @NotNull UsageInfo info1) {
    Pair<VirtualFile, Integer> offset0 = offset(info0);
    Pair<VirtualFile, Integer> offset1 = offset(info1);
    if (offset0 == null || offset0.first == null || offset1 == null || offset1.first == null || !Comparing.equal(offset0.first, offset1.first)) {
      return 0;
    }
    return offset0.second - offset1.second;
  }

  /**
   * Copy from IntelliJ sources UsageInfo.offset
   */
  private static Pair<VirtualFile, Integer> offset(UsageInfo info) {
    VirtualFile containingFile0 = info.getVirtualFile();
    int shift0 = 0;
    if (containingFile0 instanceof VirtualFileWindow) {
      shift0 = ((VirtualFileWindow)containingFile0).getDocumentWindow().injectedToHost(0);
      containingFile0 = ((VirtualFileWindow)containingFile0).getDelegate();
    }
    Segment range = info.getPsiFileRange() == null ? info.getSmartPointer().getPsiRange() : info.getPsiFileRange().getPsiRange();
    if (range == null) return null;
    return Pair.create(containingFile0, range.getStartOffset() + shift0);
  }

  @NotNull
  private Collection<UsageInfo> loadTestClass() {
    return myFixture.testFindUsages(getBasePath() + getTestName(false) + ".java");
  }

  @Override
  protected String getBasePath() {
    return super.getBasePath() + "/usage/";
  }
}
