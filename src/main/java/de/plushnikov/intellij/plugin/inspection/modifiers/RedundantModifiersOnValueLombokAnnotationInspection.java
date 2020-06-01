package de.plushnikov.intellij.plugin.inspection.modifiers;

import lombok.Value;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.PsiModifier.*;

/**
 * @author Rowicki Michał
 */
public class RedundantModifiersOnValueLombokAnnotationInspection extends LombokRedundantModifierInspection {

  public RedundantModifiersOnValueLombokAnnotationInspection() {
    super(
      Value.class,
      new RedundantModifiersInfo(RedundantModifiersInfoType.CLASS, null, "@Value already marks the class final.", FINAL),
      new RedundantModifiersInfo(RedundantModifiersInfoType.FIELD, STATIC, "@Value already marks non-static fields final.", FINAL),
      new RedundantModifiersInfo(RedundantModifiersInfoType.FIELD, STATIC, "@Value already marks non-static, package-local fields private.", PRIVATE));
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getDisplayName() {
    return "@Value modifiers";
  }
}
