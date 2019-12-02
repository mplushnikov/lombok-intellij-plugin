package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.builder.*;
import org.jetbrains.annotations.NotNull;

public class DelombokJresSuperBuilderAction extends AbstractDelombokAction {

  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresSuperBuilderClassProcessor.class),
      ServiceManager.getService(JresSuperBuilderProcessor.class));
  }
}
