package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.JresDataProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokJresDataAction extends AbstractDelombokAction {

  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ServiceManager.getService(JresDataProcessor.class));
  }
}
