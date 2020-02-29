package de.plushnikov.intellij.plugin;

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

public class LombokTestUtil {
  private static final String LOMBOK_LIBRARY_DIRECTORY = "lib";
  private static final String LOMBOK_JAR_NAME = "lombok-1.18.12.jar";

  public static void loadLombokLibrary(@NotNull JavaCodeInsightTestFixture fixture, @NotNull Module module) {
    final String lombokLibPath = PathUtil.toSystemIndependentName(new File(LOMBOK_LIBRARY_DIRECTORY).getAbsolutePath());
    addLibrary(fixture, module, "Lombok Library", lombokLibPath, LOMBOK_JAR_NAME);
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
  private static void addLibrary(JavaCodeInsightTestFixture parent, Module module, String libName, String libPath, String jarArr) {
    VfsRootAccess.allowRootAccess(libPath);
    PsiTestUtil.addLibrary(module, libName, libPath, jarArr);
  }
}
