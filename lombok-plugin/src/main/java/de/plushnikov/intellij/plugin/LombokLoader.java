package de.plushnikov.intellij.plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import de.plushnikov.intellij.plugin.extension.LombokLightMethodTreeGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * Main application component, that loads Lombok support
 */
public class LombokLoader implements ApplicationComponent {
  private static final Logger LOG = Logger.getInstance(LombokLoader.class.getName());

  @NotNull
  @Override
  public String getComponentName() {
    return "Lombok plugin for IntelliJ";
  }

  @Override
  public void initComponent() {
    LOG.info("Lombok plugin initialized for IntelliJ");
    ChangeUtil.registerTreeGenerator(new LombokLightMethodTreeGenerator());
  }

  @Override
  public void disposeComponent() {
    LOG.info("Lombok plugin disposed for IntelliJ");
  }
}
