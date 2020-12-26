package de.plushnikov.intellij.plugin.hack;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

public class Injector {

  private static volatile transient @Nullable Instrumentation instrumentation;

  public static boolean initialized() { return instrumentation != null; }

  public static Instrumentation instrumentation() { return instrumentation == null ? injectAgent() : instrumentation; }

  private static synchronized @Nullable Instrumentation injectAgent() {
    if (instrumentation == null) {
      try {
        AgentInjector.inject(Injector.class.getName(), LiveInjector.class);
        // May not be loaded by SystemClassLoader
        final Class<?> classLiveInjector = ClassLoader.getSystemClassLoader().loadClass(LiveInjector.class.getName());
        final Field fieldInstrumentation = classLiveInjector.getField("instrumentation");
        instrumentation = (Instrumentation) fieldInstrumentation.get(null);
      } catch (Exception e) { throw new InternalError("can't inject instrumentation instance", e); }
    }
    return instrumentation;
  }

}
