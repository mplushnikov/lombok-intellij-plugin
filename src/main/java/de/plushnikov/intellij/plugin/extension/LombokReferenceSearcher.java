package de.plushnikov.intellij.plugin.extension;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Annas example: org.jetbrains.plugins.javaFX.fxml.refs.JavaFxControllerFieldSearcher
 * Alternative Implementation for LombokFieldFindUsagesHandlerFactory
 */
public class LombokReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

  public LombokReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    PsiElement refElement = queryParameters.getElementToSearch();

    if (refElement instanceof PsiField) {
      searchInContainingClass((PsiField) refElement, consumer);
    }

    // Piece of code that works when using FindUsages:
    // MethodReferencesSearch.search(superMethod, parent.getScope().toSearchScope(), true)
    //
    // return MethodReferencesSearch
    //          .search(new MethodReferencesSearch.SearchParameters(method, searchScope, strictSignatureSearch, options.fastTrack))
    //          .forEach(new ReadActionProcessor<PsiReference>() {
    //            @Override
    //            public boolean processInReadAction(final PsiReference ref) {
    //              return addResult(ref, options, processor);
    //            }
    //          });
  }

  private void searchInContainingClass(final PsiField refPsiField, Processor<? super PsiReference> consumer) {
    final PsiClass containingClass = refPsiField.getContainingClass();
    if (null != containingClass) {
      boolean mayContinueSearching = searchInClassMethods(containingClass, refPsiField, consumer);

      // TODO : look in generated inner classes methods (like Builders)
    }
  }

  private boolean searchInClassMethods(PsiClass containingClass, PsiField field, Processor<? super PsiReference> consumer) {
    for(PsiMethod method : containingClass.getMethods()){
      if(method instanceof LombokLightMethodBuilder && (method.getNavigationElement() == field || method.isConstructor())){
        // only look in the methods we have generated ourselves. Other methods are already indexed and the references they contain can
        // already be found by the native ReferenceSearch
        if(!reportReferences(method, field, consumer)){
          return false;
        }
      }
    }
    return true;
  }

  private boolean reportReferences(PsiElement haystack, PsiField needle, Processor<? super PsiReference> consumer) {
    PsiReference ref = haystack.getReference();
    if (ref != null && ref.isReferenceTo(needle)) {
      return consumer.process(ref);
    }
    PsiElement[] children = haystack.getChildren();
    for (PsiElement child : children) {
      if(!reportReferences(child, needle, consumer)){
        return false;
      }
    }

    return true;
  }
}
