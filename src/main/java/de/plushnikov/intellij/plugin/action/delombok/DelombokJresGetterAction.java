package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.JresGetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.JresGetterFieldProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokJresGetterAction extends AbstractDelombokAction {

  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ServiceManager.getService(JresGetterProcessor.class),
      ServiceManager.getService(JresGetterFieldProcessor.class));
  }
}
