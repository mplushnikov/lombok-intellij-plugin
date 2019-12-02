package de.plushnikov.intellij.plugin.action.lombok;

import com.hundsun.jres.studio.annotation.JRESEqualsAndHashCode;
import com.hundsun.jres.studio.annotation.JRESGetter;
import com.hundsun.jres.studio.annotation.JRESSetter;
import com.hundsun.jres.studio.annotation.JRESToString;
import com.hundsun.jres.studio.annotation.JRESData;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

public class LombokJresDataHandler extends BaseLombokHandler {

  private final BaseLombokHandler[] handlers;

  public LombokJresDataHandler() {
    handlers = new BaseLombokHandler[]{
      new LombokJresGetterHandler(), new LombokJresGetterHandler(),
      new LombokJresToStringHandler(), new LombokJresEqualsAndHashcodeHandler()};
  }

  protected void processClass(@NotNull PsiClass psiClass) {
    for (BaseLombokHandler handler : handlers) {
      handler.processClass(psiClass);
    }

    removeDefaultAnnotation(psiClass, JRESGetter.class);
    removeDefaultAnnotation(psiClass, JRESSetter.class);
    removeDefaultAnnotation(psiClass, JRESToString.class);
    removeDefaultAnnotation(psiClass, JRESEqualsAndHashCode.class);

    addAnnotation(psiClass, JRESData.class);
  }

}
