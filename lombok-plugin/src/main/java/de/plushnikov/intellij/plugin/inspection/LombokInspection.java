package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReferenceExpression;
import de.plushnikov.intellij.plugin.extension.LombokProcessorExtensionPoint;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.processor.Processor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Plushnikov Michail
 */
public class LombokInspection extends BaseJavaLocalInspectionTool {
  private static final Logger LOG = Logger.getInstance(LombokInspection.class.getName());

  private final Map<String, Collection<Processor>> allProblemHandlers;

  public LombokInspection() {
    allProblemHandlers = new THashMap<String, Collection<Processor>>();
    for (Processor lombokInspector : LombokProcessorExtensionPoint.EP_NAME.getExtensions()) {
      Collection<Processor> inspectorCollection = allProblemHandlers.get(lombokInspector.getSupportedAnnotation());
      if (null == inspectorCollection) {
        inspectorCollection = new ArrayList<Processor>(2);
        allProblemHandlers.put(lombokInspector.getSupportedAnnotation(), inspectorCollection);
      }
      inspectorCollection.add(lombokInspector);

      LOG.debug(String.format("LombokInspection registered %s inspector", lombokInspector));
    }
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Lombok annotations inspection";
  }

  @NotNull
  @Override
  public String getGroupDisplayName() {
    return GroupNames.BUGS_GROUP_NAME;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "Lombok";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        // do nothing, just implement
      }

      @Override
      public void visitAnnotation(PsiAnnotation annotation) {
        super.visitAnnotation(annotation);

        final String qualifiedName = annotation.getQualifiedName();
        if (null != qualifiedName && allProblemHandlers.containsKey(qualifiedName)) {
          for (Processor inspector : allProblemHandlers.get(qualifiedName)) {
            Collection<LombokProblem> problems = inspector.verifyAnnotation(annotation);
            for (LombokProblem problem : problems) {
              holder.registerProblem(annotation, problem.getMessage(), problem.getHighlightType(), problem.getQuickFixes());
            }
          }
        }
      }
    };
  }
}
