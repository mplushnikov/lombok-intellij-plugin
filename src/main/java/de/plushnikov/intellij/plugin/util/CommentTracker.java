/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.plushnikov.intellij.plugin.util;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper class to implement quick-fix which collects removed comments from the PSI and can restore them at once.
 * <p>
 * After this object restores comments, it becomes unusable.
 *
 * @author Tagir Valeev
 */
public class CommentTracker {
  private Set<PsiElement> ignoredParents = new HashSet<>();
  private List<PsiComment> comments = new ArrayList<>();

  /**
   * Replaces given PsiElement collecting all the comments inside it.
   *
   * @param element     element to replace
   * @param replacement replacement element
   * @return the element which was actually inserted in the tree (either {@code replacement} or its copy)
   */
  public @NotNull
  PsiElement replace(@NotNull PsiElement element, @NotNull PsiElement replacement) {
    grabComments(element);
    return element.replace(replacement);
  }

  /**
   * Creates a replacement element from the text and replaces given element,
   * collecting all the comments inside it.
   *
   * <p>
   * The type of the created replacement will mimic the type of supplied element.
   * Supported element types are: {@link PsiExpression}, {@link PsiStatement},
   * {@link PsiTypeElement}, {@link PsiIdentifier}, {@link PsiComment}.
   * </p>
   *
   * @param element element to replace
   * @param text    replacement text
   * @return the element which was actually inserted in the tree
   */
  public @NotNull
  PsiElement replace(@NotNull PsiElement element, @NotNull String text) {
    PsiElement replacement = createElement(element, text);
    return replace(element, replacement);
  }

  /**
   * Replaces given PsiElement collecting all the comments inside it and restores comments putting them
   * to the appropriate place before replaced element.
   *
   * <p>After calling this method the tracker cannot be used anymore.</p>
   *
   * @param element     element to replace
   * @param replacement replacement element
   * @return the element which was actually inserted in the tree (either {@code replacement} or its copy)
   */
  public @NotNull
  PsiElement replaceAndRestoreComments(@NotNull PsiElement element, @NotNull PsiElement replacement) {
    PsiElement result = replace(element, replacement);
    PsiElement anchor = PsiTreeUtil.getNonStrictParentOfType(result, PsiStatement.class, PsiLambdaExpression.class, PsiVariable.class);
    if (anchor instanceof PsiLambdaExpression && anchor != result) {
      anchor = ((PsiLambdaExpression) anchor).getBody();
    }
    if (anchor instanceof PsiVariable && anchor.getParent() instanceof PsiDeclarationStatement) {
      anchor = anchor.getParent();
    }
    if (anchor == null) anchor = result;
    insertCommentsBefore(anchor);
    return result;
  }

  @NotNull
  private static PsiElement createElement(@NotNull PsiElement element, @NotNull String text) {
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(element.getProject());
    PsiElement replacement;
    if (element instanceof PsiExpression) {
      replacement = factory.createExpressionFromText(text, element);
    } else if (element instanceof PsiStatement) {
      replacement = factory.createStatementFromText(text, element);
    } else if (element instanceof PsiTypeElement) {
      replacement = factory.createTypeElementFromText(text, element);
    } else if (element instanceof PsiIdentifier) {
      replacement = factory.createIdentifier(text);
    } else if (element instanceof PsiComment) {
      replacement = factory.createCommentFromText(text, element);
    } else {
      throw new IllegalArgumentException("Unsupported element type: " + element);
    }
    return replacement;
  }

  /**
   * Inserts gathered comments just before given anchor element
   *
   * <p>After calling this method the tracker cannot be used anymore.</p>
   *
   * @param anchor
   */
  public void insertCommentsBefore(@NotNull PsiElement anchor) {
    checkState();
    if (!comments.isEmpty()) {
      PsiElement parent = anchor.getParent();
      PsiElementFactory factory = JavaPsiFacade.getElementFactory(anchor.getProject());
      for (PsiComment comment : comments) {
        if (shouldIgnore(comment)) continue;
        PsiElement added = parent.addBefore(factory.createCommentFromText(comment.getText(), anchor), anchor);
        PsiElement prevSibling = added.getPrevSibling();
        if (prevSibling instanceof PsiWhiteSpace) {
          ASTNode whiteSpaceBefore = normalizeWhiteSpace((PsiWhiteSpace) prevSibling);
          PsiElement prev = anchor.getPrevSibling();
          parent.getNode().addChild(whiteSpaceBefore, anchor.getNode());
          if (prev instanceof PsiWhiteSpace) {
            prev.delete();
          }
        }
      }
    }
    comments = null;
  }

  @NotNull
  private static ASTNode normalizeWhiteSpace(PsiWhiteSpace whiteSpace) {
    String text = whiteSpace.getText();
    int endLPos = text.lastIndexOf('\n');
    if (text.lastIndexOf('\n', endLPos - 1) >= 0) {
      // has at least two line breaks
      return ASTFactory.whitespace(text.substring(endLPos));
    }
    return ASTFactory.whitespace(text);
  }

  private boolean shouldIgnore(PsiComment comment) {
    return ignoredParents.stream().anyMatch(p -> PsiTreeUtil.isAncestor(p, comment, false));
  }

  private void grabComments(PsiElement element) {
    checkState();
    for (PsiComment comment : PsiTreeUtil.collectElementsOfType(element, PsiComment.class)) {
      if (!shouldIgnore(comment)) {
        comments.add(comment);
      }
    }
  }

  private void checkState() {
    if (comments == null) {
      throw new IllegalStateException(getClass().getSimpleName() + " has been already used");
    }
  }

}
