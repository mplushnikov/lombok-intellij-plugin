package de.plushnikov.intellij.plugin.hack;

import java.lang.instrument.Instrumentation;

public class LiveInjector {

  public static /*@Nullable*/ String agentArgs;

  public static /*@Nullable*/ Instrumentation instrumentation;

  public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
    LiveInjector.agentArgs = agentArgs;
    LiveInjector.instrumentation = instrumentation;
  }

}
