package de.plushnikov.intellij.plugin.hack.extension;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import de.plushnikov.intellij.plugin.hack.Injector;
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.jetbrains.org.objectweb.asm.Opcodes.*;

public class ExtensionMethodSupport {

  static {
    try {
      final ClassLoader loader = PsiClassImplUtil.class.getClassLoader();
      // Since the injection target and the injection callback may not be the same class loader, they need to be called using the method handle.
      final MethodHandle handle = MethodHandles.lookup().findStatic(ExtensionMethodHandler.class, "processDeclarations",
        MethodType.methodType(boolean.class, boolean.class, PsiClass.class, PsiScopeProcessor.class, ResolveState.class, PsiElement.class, PsiElement.class));
      if (ExtensionMethodHolder.class.getClassLoader() == PsiClassImplUtil.class.getClassLoader()) // test env classloader same
        ExtensionMethodHolder.handle = handle;
      else {
        Class<?> holder;
        try {
          holder = Class.forName(ExtensionMethodHolder.class.getName(), true, loader);
        } catch (Throwable throwable) {
          final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
          defineClass.setAccessible(true);
          final byte[] bytes = getBytesFromClass(ExtensionMethodHolder.class);
          holder = (Class<?>) defineClass.invoke(loader, bytes, 0, bytes.length);
        }
        final Field handleField = holder.getField("handle");
        handleField.setAccessible(true);
        handleField.set(null, handle);
      }
      final Instrumentation instrumentation = Injector.instrumentation();
      instrumentation.addTransformer(new ClassFileTransformer() {
        @Override
        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] bytes) throws IllegalClassFormatException {
          if (classBeingRedefined == PsiClassImplUtil.class) {
            final ClassNode node = new ClassNode();
            final ClassReader reader = new ClassReader(bytes);
            reader.accept(node, 0);
            for (final MethodNode method : node.methods) {
              if (method.name.equals("processDeclarationsInClass") && method.desc.equals("(Lcom/intellij/psi/PsiClass;Lcom/intellij/psi/scope/PsiScopeProcessor;Lcom/intellij/psi/ResolveState;Ljava/util/Set;Lcom/intellij/psi/PsiElement;Lcom/intellij/psi/PsiElement;Lcom/intellij/pom/java/LanguageLevel;ZLcom/intellij/psi/search/GlobalSearchScope;)Z")) {
                final InsnList instructions = method.instructions;
                int count = 2;
                final List<AbstractInsnNode> target = new ArrayList<>();
                for (final ListIterator<AbstractInsnNode> iterator = instructions.iterator(instructions.size()); iterator.hasPrevious() && count > 0; ) {
                  final AbstractInsnNode insn = iterator.previous();
                  if (insn.getOpcode() == IRETURN) {
                    target.add(insn);
                    count--;
                  }
                }
                target.forEach(insn -> {
                  final InsnList list = new InsnList();
                  list.add(new FieldInsnNode(GETSTATIC, "de/plushnikov/intellij/plugin/hack/extension/ExtensionMethodHolder", "handle", "Ljava/lang/invoke/MethodHandle;"));
                  list.add(new InsnNode(SWAP));
                  list.add(new VarInsnNode(ALOAD, 0));
                  list.add(new VarInsnNode(ALOAD, 1));
                  list.add(new VarInsnNode(ALOAD, 2));
                  list.add(new VarInsnNode(ALOAD, 4));
                  list.add(new VarInsnNode(ALOAD, 5));
                  list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", "(ZLcom/intellij/psi/PsiClass;Lcom/intellij/psi/scope/PsiScopeProcessor;Lcom/intellij/psi/ResolveState;Lcom/intellij/psi/PsiElement;Lcom/intellij/psi/PsiElement;)Z", false));
                  instructions.insertBefore(insn, list);
                });
              }
            }
            final ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
          }
          return null;
        }
      }, true);
      instrumentation.retransformClasses(PsiClassImplUtil.class);
    } catch (Throwable throwable) { throwable.printStackTrace(); }
  }

  private static byte[] getBytesFromClass(final Class<?> clazz) throws IOException {
    try (final InputStream input = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class")) {
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      final byte[] data = new byte[1 << 12];
      while ((nRead = input.read(data, 0, data.length)) != -1)
        buffer.write(data, 0, nRead);
      buffer.flush();
      return buffer.toByteArray();
    }
  }

}
