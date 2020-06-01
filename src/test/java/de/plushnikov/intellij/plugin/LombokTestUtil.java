package de.plushnikov.intellij.plugin;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LombokTestUtil {
  private static final String THIRD_PARTY_LIB_DIRECTORY = "lib";
  private static final String LOMBOK_JAR_NAME = "lombok-1.18.12.jar";
  private static final String SLF4J_JAR_NAME = "slf4j-api-1.7.30.jar";

  public static void loadLombokLibrary(@NotNull JavaCodeInsightTestFixture projectDisposable, @NotNull Module module) {
    final String lombokLibPath = PathUtil.toSystemIndependentName(new File(LOMBOK_LIBRARY_DIRECTORY).getAbsolutePath());
    addLibrary(projectDisposable, module, "Lombok Library", lombokLibPath, LOMBOK_JAR_NAME);
  }

  public static void loadLombokLibrary(@NotNull Disposable projectDisposable, @NotNull Module module) {
    loadLibrary(projectDisposable, module, "Lombok Library", LOMBOK_JAR_NAME);
  }

  public static void loadSlf4jLibrary(@NotNull Disposable projectDisposable, @NotNull Module module) {
    loadLibrary(projectDisposable, module, "Slf4j Library", SLF4J_JAR_NAME);
  }

  private static void loadLibrary(@NotNull Disposable projectDisposable, @NotNull Module module, String libraryName,
                                  String libraryJarName) {
    final String lombokLibPath = PathUtil.toSystemIndependentName(new File(THIRD_PARTY_LIB_DIRECTORY).getAbsolutePath());
    VfsRootAccess.allowRootAccess(projectDisposable, lombokLibPath);
    PsiTestUtil.addLibrary(projectDisposable, module, libraryName, lombokLibPath, libraryJarName);
  }

  public static LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public Sdk getSdk() {
        return JavaSdk.getInstance().createJdk("java 1.8", "lib/mockJDK-1.8", false);
      }

      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(LanguageLevel.JDK_1_8);
      }
    };
  }

  /*
   * For IntelliJ >= 2017.3 we need to call PsiTestUtil.addLibrary with Disposable parameter
   * For IntelliJ <2017.3 we can use default PsiTestUtil.addLibrary version
   */
  public static void addLibrary(@NotNull JavaCodeInsightTestFixture parent, Module module, String libName, String libPath, String jarArr) {
    Disposable projectDisposable;
    try {
      final MethodHandle methodHandle = MethodHandles.lookup().findVirtual(JavaCodeInsightTestFixture.class, "getProjectDisposable", MethodType.methodType(Disposable.class));
      projectDisposable = (Disposable) methodHandle.invoke(parent);
    } catch (Throwable throwable) {
      projectDisposable = null;
    }

    if (null == projectDisposable) {
      VfsRootAccess.allowRootAccess(libPath);
      PsiTestUtil.addLibrary(module, libName, libPath, jarArr);
    } else {
      VfsRootAccess.allowRootAccess(projectDisposable, libPath);
      addLibrary(projectDisposable, module, libName, libPath, jarArr);
    }
  }

  private static void addLibrary(@NotNull Disposable projectDisposable, Module module, String libName, String libPath, String jarArr) {
    final MethodHandle methodHandle;
    try {
      methodHandle = MethodHandles.lookup().findStatic(PsiTestUtil.class, "addLibrary", MethodType.methodType(void.class,
        Disposable.class, Module.class, String.class, String.class, String[].class));
      methodHandle.invoke(projectDisposable, module, libName, libPath, new String[]{jarArr});
    } catch (Throwable throwable) {
      PsiTestUtil.addLibrary(module, libName, libPath, jarArr);
    }
  }
}
