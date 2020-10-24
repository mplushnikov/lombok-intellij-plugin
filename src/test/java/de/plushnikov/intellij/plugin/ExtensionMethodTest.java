package de.plushnikov.intellij.plugin;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiMethodReferenceExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightJavaCodeInsightTestCase;
import com.intellij.testFramework.LightProjectDescriptor;

import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;

public class ExtensionMethodTest extends LightJavaCodeInsightTestCase {

  public void testBaseTypeExtensionMethod() {
    checkMethod(createTestMethod("\"\".requireNonNull();"), true);
  }

  public void testExtensionMethodReference() {
    checkMethod(createTestMethod("Function<String, String> func = \"\"::requireNonNullElse;"), false);
  }

  public void testArrayTypeExtensionMethod() {
    checkMethod(createTestMethod("double array[] = {};\n" +
                                 "array.fill(3.0);\n" +
                                 "array.binarySearch(1.0);"), true);
  }

  public void testGenericArrayTypeExtensionMethod() {
    checkMethod(createTestMethod("new String[0].asList();new Double[0][0].setAll(i -> i);"), true);
  }

  private void checkMethod(final PsiMethod method, final boolean successful) {
    if (!PsiTreeUtil.findChildrenOfAnyType(method.getBody(), PsiMethodCallExpression.class, PsiMethodReferenceExpression.class).stream()
      .allMatch(expr -> successful == (expr instanceof PsiMethodReferenceExpression ? ((PsiMethodReferenceExpression) expr).resolve() != null : expr.getType() != null)))
      throw new AssertionFailedError(method.getText() + "\nexpected: " + successful);
  }

  private PsiMethod createTestMethod(final String body) { return PsiTreeUtil.findChildOfType(createTestFile("static void f() {" + body + "}"), PsiMethod.class); }

  private PsiFile createTestFile(final String body) {
    return createFile("test.java", "import java.util.*; import java.util.function.*;" +
                                   "@lombok.experimental.ExtensionMethod({" +
                                   "Objects.class, Arrays.class" +
                                   "}) class TestExtensionMethod { " + body + "}");
  }

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() { return LombokTestUtil.getProjectDescriptor(); }

}
