package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.JresValueProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokJresValueAction extends AbstractDelombokAction {
  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ServiceManager.getService(JresValueProcessor.class));
  }
}
