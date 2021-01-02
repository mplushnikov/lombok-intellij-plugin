package de.plushnikov.intellij.plugin.configsystem;

/**
 * Unit tests for IntelliJPlugin for Lombok with activated config system
 */
public class FieldDefaultsTest extends AbstractLombokConfigSystemTestCase {

  @Override
  protected String getBasePath() {
    return super.getBasePath() + "/configsystem/fieldDefaults";
  }

  //region DefaultFinal
  public void testDefaultFinal$DefaultFinalFieldTest() {
    doTest();
  }

  public void testDefaultFinal$DefaultFinalFieldWithFieldDefaultsTest() {
    doTest();
  }

  public void testDefaultFinal$DefaultFinalFieldWithNonFinalTest() {
    doTest();
  }

  public void testDefaultFinal$DefaultFinalFieldWithUnrelatedFieldDefaultsTest() {
    doTest();
  }
  //endregion

  //region DefaultPrivate
  public void testDefaultPrivate$DefaultPrivateFieldTest() {
    doTest();
  }

  public void testDefaultPrivate$DefaultPrivateFieldWithFieldDefaultsTest() {
    doTest();
  }

  public void testDefaultPrivate$DefaultPrivateFieldWithPackagePrivateTest() {
    doTest();
  }

  public void testDefaultPrivate$DefaultPrivateFieldWithUnrelatedFieldDefaultsTest() {
    doTest();
  }
  //endregion
}
