package de.plushnikov.intellij.plugin.action.delombok;

import de.plushnikov.intellij.plugin.processor.clazz.DataProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.EqualsAndHashCodeProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.GetterProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.SetterProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.ToStringProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.ValueProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.WitherProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderClassProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderPreDefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderPreDefinedInnerClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.*;
import de.plushnikov.intellij.plugin.processor.field.DelegateFieldProcessor;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import de.plushnikov.intellij.plugin.processor.field.SetterFieldProcessor;
import de.plushnikov.intellij.plugin.processor.field.WitherFieldProcessor;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.processor.handler.DelegateHandler;
import de.plushnikov.intellij.plugin.processor.method.BuilderClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.BuilderMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.DelegateMethodProcessor;

public class DelombokEverythingAction extends BaseDelombokAction {

  public DelombokEverythingAction() {
    super(createHandler());
  }

  private static BaseDelombokHandler createHandler() {
    final GetterFieldProcessor getterFieldProcessor = new GetterFieldProcessor();
    final GetterProcessor getterProcessor = new GetterProcessor(getterFieldProcessor);

    final SetterFieldProcessor setterFieldProcessor = new SetterFieldProcessor();
    final SetterProcessor setterProcessor = new SetterProcessor(setterFieldProcessor);

    final EqualsAndHashCodeProcessor equalsAndHashCodeProcessor = new EqualsAndHashCodeProcessor();
    final ToStringProcessor toStringProcessor = new ToStringProcessor();

    final RequiredArgsConstructorProcessor requiredArgsConstructorProcessor = new RequiredArgsConstructorProcessor();
    final AllArgsConstructorProcessor allArgsConstructorProcessor = new AllArgsConstructorProcessor();
    final NoArgsConstructorProcessor noArgsConstructorProcessor = new NoArgsConstructorProcessor();

    final DelegateHandler delegateHandler = new DelegateHandler();
    final BuilderHandler builderHandler = new BuilderHandler(toStringProcessor, noArgsConstructorProcessor);

    return new BaseDelombokHandler(true,
      requiredArgsConstructorProcessor, allArgsConstructorProcessor, noArgsConstructorProcessor,
      new DataProcessor(getterProcessor, setterProcessor, equalsAndHashCodeProcessor, toStringProcessor, requiredArgsConstructorProcessor),
      getterProcessor, new ValueProcessor(getterProcessor, equalsAndHashCodeProcessor, toStringProcessor, allArgsConstructorProcessor),
      new WitherProcessor(new WitherFieldProcessor(requiredArgsConstructorProcessor)),
      setterProcessor, equalsAndHashCodeProcessor, toStringProcessor,
      new CommonsLogProcessor(), new JBossLogProcessor(), new Log4jProcessor(), new Log4j2Processor(), new LogProcessor(), new Slf4jProcessor(), new XSlf4jProcessor(), new FloggerProcessor(),
      getterFieldProcessor, setterFieldProcessor,
      new WitherFieldProcessor(requiredArgsConstructorProcessor),
      new DelegateFieldProcessor(delegateHandler),
      new DelegateMethodProcessor(delegateHandler),

      new BuilderPreDefinedInnerClassFieldProcessor(builderHandler),
      new BuilderPreDefinedInnerClassMethodProcessor(builderHandler),
      new BuilderClassProcessor(builderHandler), new BuilderClassMethodProcessor(builderHandler),
      new BuilderMethodProcessor(builderHandler), new BuilderProcessor(allArgsConstructorProcessor, builderHandler));
  }

}
