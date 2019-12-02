package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.JresSetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.JresSetterFieldProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokJresSetterAction extends AbstractDelombokAction {
  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ServiceManager.getService(JresSetterProcessor.class),
      ServiceManager.getService(JresSetterFieldProcessor.class));
  }
}
