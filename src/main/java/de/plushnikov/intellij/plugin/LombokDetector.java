package de.plushnikov.intellij.plugin;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexej Kubarev
 */
public class LombokDetector {

  public boolean hasLombok(Project project) {
    PsiPackage lombokPackage = JavaPsiFacade.getInstance(project).findPackage("lombok");

    return lombokPackage != null;
  }

  public String getLombokVersion(@NotNull  Module module) {

    System.out.println("Performing Lombok Version check for " + module.getName());

    try {
      Method getLombokVersionMethod = getVersionMethod(module);
      if (null != getLombokVersionMethod) {
        String response = (String) getLombokVersionMethod.invoke(null);

        System.out.println("Got lombok version: " + response);
        return response;
      } else {
        System.out.println("Falling back to alternative method");
        return getAlternativeVersion(module);
      }
    } catch (InvocationTargetException ex) {
      System.err.println("Can't call constuctor! " + ex.getMessage());
    } catch (IllegalAccessException ex) {
      System.err.println("Can't call constuctor! " + ex.getMessage());
    }

    return null;
  }


  //<editor-fold desc="Reflective Version Check">

  private Method getVersionMethod(Module module) {

    Set<URL> urls = getDependencyURLs(module);
    ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));

    try {

      ClassLoader instance = getShadowClassLoader(cl);
      if (null != instance) {
        Class<?> lombokVersionClass = instance.loadClass("lombok.core.Version");
        return lombokVersionClass.getMethod("getVersion");
      }

    } catch (NoSuchMethodException ex) {
      System.err.println("Can't load constuctor! " + ex.getMessage());
    } catch (ClassNotFoundException ex) {
      System.err.println("Class not found " + ex.getMessage());
    }

    return null;
  }

  private ClassLoader getShadowClassLoader(ClassLoader parent) {
    try {
      Class<?> clazz = parent.loadClass("lombok.launch.ShadowClassLoader");
      Constructor constr = clazz.getDeclaredConstructor(ClassLoader.class, String.class, String.class, List.class, List.class);
      constr.setAccessible(true);

      return (ClassLoader) constr.newInstance(parent, "lombok", null, null, null);
    } catch (InstantiationException ex) {
      System.err.println("Can't call constuctor! " + ex.getMessage());
    } catch (InvocationTargetException ex) {
      System.err.println("Can't call constuctor! " + ex.getMessage());
    } catch (IllegalAccessException ex) {
      System.err.println("Can't call constuctor! " + ex.getMessage());
    } catch (NoSuchMethodException ex) {
      System.err.println("Can't load constuctor! " + ex.getMessage());
    } catch (ClassNotFoundException ex) {
      System.err.println("Class not found " + ex.getMessage());
    }

    return null;
  }

  private Set<URL> getDependencyURLs(Module module) {
    final Set<URL> urls = new HashSet<URL>();

    ModuleRootManager mrm = ModuleRootManager.getInstance(module);
    for (OrderEntry entry : mrm.getOrderEntries()) {
      VirtualFile[] files = entry.getFiles(OrderRootType.CLASSES);

      for (VirtualFile file : files) {
        try {
          String path = "file://" + file.getCanonicalPath();

          int idx = path.indexOf('!');
          if (idx != -1) {
            path = path.substring(0, idx);
          }

          urls.add(new URL(path));
        } catch (MalformedURLException ex) {
          System.err.println("Bad URL! " + ex.getMessage());
        }
      }
    }

    return urls;
  }

  //</editor-fold>

  //<editor-fold desc="Alternative Version Check">

  private String getAlternativeVersion(Module module) {

    final OrderEntry lombokEntry = findLombokEntry(ModuleRootManager.getInstance(module));
    return parseLombokVersion(lombokEntry);
  }

  @Nullable
  private OrderEntry findLombokEntry(@NotNull ModuleRootManager moduleRootManager) {
    final OrderEntry[] orderEntries = moduleRootManager.getOrderEntries();
    for (OrderEntry orderEntry : orderEntries) {
      if (orderEntry.getPresentableName().contains("lombok")) {
        return orderEntry;
      }
    }
    return null;
  }

  @Nullable
  private String parseLombokVersion(@Nullable OrderEntry orderEntry) {
    String result = null;
    if (null != orderEntry) {
      final String presentableName = orderEntry.getPresentableName();
      Pattern pattern = Pattern.compile("(.*:)([\\d\\.]+)(.*)");
      final Matcher matcher = pattern.matcher(presentableName);
      if (matcher.find()) {
        result = matcher.group(2);
      }
    }
    return result;
  }

  //</editor-fold>

}
