package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.JresEqualsAndHashCodeProcessor;

public class DelombokJresEqualsAndHashCodeAction extends AbstractDelombokAction {

  @Override
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ServiceManager.getService(JresEqualsAndHashCodeProcessor.class));
  }
}
