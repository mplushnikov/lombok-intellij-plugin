package de.plushnikov.intellij.plugin;

import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Field;

/**
 * @author Plushnikov Michail
 */
public final class ReflectionUtil {
  private static final Logger LOG = Logger.getInstance(ReflectionUtil.class.getName());

  public static <T, R> R getFinalFieldPerReflection(Class<T> clazz, T instance, Class<R> fieldClass) {
    try {
      for (Field field : clazz.getDeclaredFields()) {
        if (field.getType().equals(fieldClass)) {
          field.setAccessible(true);
          return (R) field.get(instance);
        }
      }
    } catch (IllegalArgumentException | IllegalAccessException x) {
      LOG.error(x);
    }
    return null;
  }
}
