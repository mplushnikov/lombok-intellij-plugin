package de.plushnikov.intellij.plugin.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightParameterListBuilder;

import java.util.Arrays;

public class LombokLightParameterListBuilder extends LightParameterListBuilder {
  private static final Logger LOG = Logger.getInstance(LombokLightParameterListBuilder.class);

  public LombokLightParameterListBuilder(PsiManager manager, Language language) {
    super(manager, language);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LombokLightParameterListBuilder that = (LombokLightParameterListBuilder) o;

    if (getParametersCount() != that.getParametersCount()) {
      return false;
    }

    if(getNavigationElement() != this && !getNavigationElement().equals(that.getNavigationElement())) {
      if(Arrays.equals(getParameters(), that.getParameters())) {
        LOG.warn("Usually I would have been equal!");
      }
      return false;
    }

    return Arrays.equals(getParameters(), that.getParameters());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getParameters());
  }
}
