package de.plushnikov.lombok.tests;

import de.plushnikov.lombok.LombokParsingTestCase;

import java.io.IOException;

/**
 * Unit tests for IntelliJPlugin for Lombok, based on lombok test classes
 * For this to work, the correct system property idea.home.path needs to be passed to the test runner.
 */
public class SetterTestCase extends LombokParsingTestCase {

  public void testSetterAccessLevel() throws IOException {
    doTest();
  }

  public void testSetterAlreadyExists() throws IOException {
    doTest();
  }

  public void testSetterDeprecated() throws IOException {
    doTest();
  }

  public void testSetterOnClass() throws IOException {
    doTest();
  }

  public void testSetterOnMethodOnParam() throws IOException {
    doTest();
  }

  public void testSetterOnStatic() throws IOException {
    doTest();
  }

  public void testSetterPlain() throws IOException {
    doTest();
  }

  public void testSetterWithDollar() throws IOException {
    doTest();
  }
}