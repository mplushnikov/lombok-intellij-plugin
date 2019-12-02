package de.plushnikov.intellij.plugin.action.delombok;

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

public class DelombokEverythingAction extends AbstractDelombokAction {

  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
      ServiceManager.getService(RequiredArgsConstructorProcessor.class),
      ServiceManager.getService(AllArgsConstructorProcessor.class),
      ServiceManager.getService(NoArgsConstructorProcessor.class),

      ServiceManager.getService(DataProcessor.class),
      ServiceManager.getService(GetterProcessor.class),
      ServiceManager.getService(ValueProcessor.class),
      ServiceManager.getService(WitherProcessor.class),
      ServiceManager.getService(SetterProcessor.class),
      ServiceManager.getService(EqualsAndHashCodeProcessor.class),
      ServiceManager.getService(ToStringProcessor.class),

      ServiceManager.getService(JresDataProcessor.class),
      ServiceManager.getService(JresGetterProcessor.class),
      ServiceManager.getService(JresValueProcessor.class),
      ServiceManager.getService(JresWitherProcessor.class),
      ServiceManager.getService(JresSetterProcessor.class),
      ServiceManager.getService(JresEqualsAndHashCodeProcessor.class),
      ServiceManager.getService(JresToStringProcessor.class),


      ServiceManager.getService(CommonsLogProcessor.class), ServiceManager.getService(JBossLogProcessor.class), ServiceManager.getService(Log4jProcessor.class),
      ServiceManager.getService(Log4j2Processor.class), ServiceManager.getService(LogProcessor.class), ServiceManager.getService(Slf4jProcessor.class),
      ServiceManager.getService(XSlf4jProcessor.class), ServiceManager.getService(FloggerProcessor.class), ServiceManager.getService(CustomLogProcessor.class),

      ServiceManager.getService(GetterFieldProcessor.class),
      ServiceManager.getService(SetterFieldProcessor.class),
      ServiceManager.getService(WitherFieldProcessor.class),
      ServiceManager.getService(DelegateFieldProcessor.class),
      ServiceManager.getService(DelegateMethodProcessor.class),

      ServiceManager.getService(JresGetterFieldProcessor.class),
      ServiceManager.getService(JresSetterFieldProcessor.class),
      ServiceManager.getService(JresWitherFieldProcessor.class),

      ServiceManager.getService(FieldNameConstantsOldProcessor.class),
      ServiceManager.getService(FieldNameConstantsFieldProcessor.class),
      ServiceManager.getService(FieldNameConstantsProcessor.class),
      ServiceManager.getService(FieldNameConstantsPredefinedInnerClassFieldProcessor.class),

      ServiceManager.getService(UtilityClassProcessor.class),

      ServiceManager.getService(BuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(BuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(BuilderClassProcessor.class),
      ServiceManager.getService(BuilderClassMethodProcessor.class),
      ServiceManager.getService(BuilderMethodProcessor.class),
      ServiceManager.getService(BuilderProcessor.class),

      ServiceManager.getService(JresBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderClassProcessor.class),
      ServiceManager.getService(JresBuilderClassMethodProcessor.class),
      ServiceManager.getService(JresBuilderMethodProcessor.class),
      ServiceManager.getService(JresBuilderProcessor.class),

      ServiceManager.getService(SuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(SuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(SuperBuilderClassProcessor.class),
      ServiceManager.getService(SuperBuilderProcessor.class),

      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ServiceManager.getService(JresSuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ServiceManager.getService(JresSuperBuilderClassProcessor.class),
      ServiceManager.getService(JresSuperBuilderProcessor.class));
  }

}
