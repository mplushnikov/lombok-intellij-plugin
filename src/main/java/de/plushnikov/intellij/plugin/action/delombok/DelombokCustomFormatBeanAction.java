package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.CustomFormatBeanProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * @author lihongbin
 */
public class DelombokCustomFormatBeanAction extends AbstractDelombokAction {
  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ServiceManager.getService(CustomFormatBeanProcessor.class));
  }
}
