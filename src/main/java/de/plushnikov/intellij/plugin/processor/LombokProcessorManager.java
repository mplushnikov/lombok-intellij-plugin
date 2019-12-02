package de.plushnikov.intellij.plugin.processor;

import com.intellij.openapi.components.ServiceManager;
import de.plushnikov.intellij.plugin.processor.clazz.*;
import de.plushnikov.intellij.plugin.processor.clazz.builder.*;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsOldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsPredefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.CommonsLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.CustomLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.FloggerProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.JBossLogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Log4j2Processor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Log4jProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.LogProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.Slf4jProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.XSlf4jProcessor;
import de.plushnikov.intellij.plugin.processor.field.*;
import de.plushnikov.intellij.plugin.processor.method.*;
import de.plushnikov.intellij.plugin.processor.modifier.FieldDefaultsModifierProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.ModifierProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.UtilityClassModifierProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.ValModifierProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.ValueModifierProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class LombokProcessorManager {
  @NotNull
  public static Collection<Processor> getLombokProcessors() {
    return Arrays.asList(
      ServiceManager.getService(AllArgsConstructorProcessor.class),
      ServiceManager.getService(NoArgsConstructorProcessor.class),
      ServiceManager.getService(RequiredArgsConstructorProcessor.class),

      ServiceManager.getService(LogProcessor.class),
      ServiceManager.getService(Log4jProcessor.class),
      ServiceManager.getService(Log4j2Processor.class),
      ServiceManager.getService(Slf4jProcessor.class),
      ServiceManager.getService(XSlf4jProcessor.class),
      ServiceManager.getService(CommonsLogProcessor.class),
      ServiceManager.getService(JBossLogProcessor.class),
      ServiceManager.getService(FloggerProcessor.class),
      ServiceManager.getService(CustomLogProcessor.class),

      ServiceManager.getService(DataProcessor.class),
      ServiceManager.getService(EqualsAndHashCodeProcessor.class),
      ServiceManager.getService(GetterProcessor.class),
      ServiceManager.getService(SetterProcessor.class),
      ServiceManager.getService(ToStringProcessor.class),
      ServiceManager.getService(WitherProcessor.class),

      ServiceManager.getService(JresDataProcessor.class),
      ServiceManager.getService(JresEqualsAndHashCodeProcessor.class),
      ServiceManager.getService(JresGetterProcessor.class),
      ServiceManager.getService(JresSetterProcessor.class),
      ServiceManager.getService(JresToStringProcessor.class),
      ServiceManager.getService(JresWitherProcessor.class),

      ServiceManager.getService(BuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(BuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(BuilderClassProcessor.class),
      ServiceManager.getService(BuilderProcessor.class),
      ServiceManager.getService(BuilderClassMethodProcessor.class),
      ServiceManager.getService(BuilderMethodProcessor.class),

      ServiceManager.getService(JresBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderClassProcessor.class),
      ServiceManager.getService(JresBuilderProcessor.class),
      ServiceManager.getService(JresBuilderClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderMethodProcessor.class),

      ServiceManager.getService(SuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(SuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(SuperBuilderClassProcessor.class),
      ServiceManager.getService(SuperBuilderProcessor.class),

      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresSuperBuilderClassProcessor.class),
      ServiceManager.getService(JresSuperBuilderProcessor.class),

      ServiceManager.getService(ValueProcessor.class),
      ServiceManager.getService(JresValueProcessor.class),

      ServiceManager.getService(UtilityClassProcessor.class),

      ServiceManager.getService(FieldNameConstantsOldProcessor.class),
      ServiceManager.getService(FieldNameConstantsFieldProcessor.class),

      ServiceManager.getService(FieldNameConstantsProcessor.class),
      ServiceManager.getService(FieldNameConstantsPredefinedInnerClassFieldProcessor.class),

      ServiceManager.getService(DelegateFieldProcessor.class),
      ServiceManager.getService(GetterFieldProcessor.class),
      ServiceManager.getService(SetterFieldProcessor.class),
      ServiceManager.getService(WitherFieldProcessor.class),

      ServiceManager.getService(JresGetterFieldProcessor.class),
      ServiceManager.getService(JresSetterFieldProcessor.class),
      ServiceManager.getService(JresWitherFieldProcessor.class),

      ServiceManager.getService(DelegateMethodProcessor.class),

      ServiceManager.getService(CleanupProcessor.class)
//      ,ServiceManager.getService(SynchronizedProcessor.class)
    );
  }

  @NotNull
  public static Collection<ModifierProcessor> getLombokModifierProcessors() {
    return Arrays.asList(
      ServiceManager.getService(FieldDefaultsModifierProcessor.class),
      ServiceManager.getService(UtilityClassModifierProcessor.class),
      ServiceManager.getService(ValModifierProcessor.class),
      ServiceManager.getService(ValueModifierProcessor.class));
  }
}
