package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.openapi.actionSystem.AnAction;
import de.plushnikov.intellij.plugin.action.LombokLightActionTestCase;

public class LombokCustomFormatBeanActionTest extends LombokLightActionTestCase {


  protected AnAction getAction() {
    return new LombokCustomFormatBeanAction();
  }

  @Override
  protected String getBasePath() {
    return super.getBasePath() + "/action/lombok/convertable";
  }

  public void testConvertableSimple() throws Exception {
    doTest();
  }
}
