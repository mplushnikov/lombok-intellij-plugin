package de.plushnikov.intellij.plugin.action.lombok;

/**
 * @author lihongbin
 */
public class LombokCustomFormatBeanAction extends BaseLombokAction {

  public LombokCustomFormatBeanAction() {
    super(new LombokCustomFormatBeanHandler());
  }

}
