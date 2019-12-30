package de.plushnikov.intellij.plugin.extension;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class LombokMethodReferenceSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {

  public LombokMethodReferenceSearcher(boolean requireReadAction) {
    super(requireReadAction);
    System.out.println("LombokMethodReferenceSearcher(boolean)");
  }

  public LombokMethodReferenceSearcher() {
    System.out.println("LombokMethodReferenceSearcher()");
  }

  @Override
  public void processQuery(@NotNull MethodReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    PsiMethod method = queryParameters.getMethod();
    System.out.println("method: " + method);
  }

}
