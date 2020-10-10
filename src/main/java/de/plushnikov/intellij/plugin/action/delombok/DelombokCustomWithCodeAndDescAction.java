package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc.WithCodeAndDescConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc.WithCodeAndDescFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.enumcodeanddesc.WithCodeAndDescMethodProcessor;
import org.jetbrains.annotations.NotNull;

public class DelombokCustomWithCodeAndDescAction extends AbstractDelombokAction {
  @NotNull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ServiceManager.getService(WithCodeAndDescFieldProcessor.class),
      ServiceManager.getService(WithCodeAndDescConstructorProcessor.class),
      ServiceManager.getService(WithCodeAndDescMethodProcessor.class));
  }
}
