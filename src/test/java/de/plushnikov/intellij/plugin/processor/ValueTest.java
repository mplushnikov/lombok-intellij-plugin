package de.plushnikov.intellij.plugin.processor;

import de.plushnikov.intellij.plugin.AbstractLombokParsingTestCase;

import java.io.IOException;

public class ValueTest extends AbstractLombokParsingTestCase {

  protected boolean shouldCompareCodeBlocks() {
    return false;
  }

  public void testValue$ValueIssue78() {
    doTest(true);
  }

  public void testValue$ValueIssue94() {
    doTest(true);
  }

  public void testValue$ValuePlain() {
    doTest(true);
  }

  public void testValue$ValueExperimental() {
    doTest(true);
  }

  public void testValue$ValueExperimentalStarImport() {
    doTest(true);
  }

  public void testValue$ValueBuilder() {
    doTest(true);
  }

  public void testValue$ValueAndBuilder93() {
    doTest(true);
  }

  public void testValue$ValueAndWither() {
    doTest(true);
  }

  public void testValue$ValueAndWitherAndRequiredConstructor() {
    doTest(true);
  }

  public void testValue$ValueWithGeneric176() {
    doTest(true);
  }

  public void testValue$ValueWithPackagePrivate() {
    doTest(true);
  }
}
