package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.builder.*;
import de.plushnikov.intellij.plugin.processor.method.JresBuilderClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.JresBuilderMethodProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokJresBuilderAction extends AbstractDelombokAction {

  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
      ServiceManager.getService(JresBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderClassProcessor.class),
      ServiceManager.getService(JresBuilderClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderMethodProcessor.class),
      ServiceManager.getService(JresBuilderProcessor.class));
  }
}
