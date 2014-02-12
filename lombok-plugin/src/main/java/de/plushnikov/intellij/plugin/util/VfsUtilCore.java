package de.plushnikov.intellij.plugin.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Backport/copy of Intellij 11 File
 */
public class VfsUtilCore {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vfs.VfsUtilCore");

  public static void visitChildrenRecursively(@NotNull VirtualFile file, @NotNull VirtualFileVisitor visitor) {
    visitChildrenRecursively(file, visitor, null);
  }

  private static void visitChildrenRecursively(@NotNull VirtualFile file,
                                               @NotNull VirtualFileVisitor visitor,
                                               @Nullable Set<VirtualFile> visitedSymlinks) {

    if (!file.isValid()) return;
    if (!visitor.visitFile(file)) return;
//    if (file.isSymLink()) {
//      if (visitedSymlinks == null) {
//        visitedSymlinks = new HashSet<VirtualFile>();
//      }
//      if (!visitedSymlinks.add(file)) {
//        visitor.afterChildrenVisited(file);
//        return;
//      }
//    }
    VirtualFile[] children = file.getChildren();
    for (VirtualFile child : children) {
      visitChildrenRecursively(child, visitor, visitedSymlinks);
    }
    visitor.afterChildrenVisited(file);
  }

}
