package de.plushnikov.intellij.plugin.processor;

import java.util.Collection;

/**
 * Interface extension point to allow dependent plugins to register
 * own annotation processors with the Lombok plugin processing.
 */
public interface LombokProcessorProvider {

  default void addAllProcessors(final Collection<Processor> registeredProcessors) {}

  default void addModifierProcessors(final Collection<Processor> registeredModifierProcessors) {}
}
