package de.plushnikov.intellij.plugin.extension;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Processor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
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
    PsiMethod queryMethod = queryParameters.getMethod();
    LombokLightMethodBuilder builder = LombokLightMethodBuilder.getLightMethodBuilder(queryMethod);
    if (builder != null) {
      // Each Lombok-generated PsiMethod exists twice.
      // - Once as an instance of LombokLightMethodBuilder which is properly wired by to its containing class.
      // - Once as a degenerated instance of PsiMethodImpl which is built by LombokLightMethodBuilder and is not properly wired to its containing class
      // We replace the search for the degenerated PsiMethodImpl by a search for the rich LombokLightMethodBuilder
      MethodReferencesSearch.SearchParameters newParameters = new MethodReferencesSearch.SearchParameters(
        builder,
        queryParameters.getScopeDeterminedByUser(),
        queryParameters.isStrictSignatureSearch(),
        queryParameters.getOptimizer());
      MethodReferencesSearch.search(newParameters).forEach(consumer);
    }
  }
}
