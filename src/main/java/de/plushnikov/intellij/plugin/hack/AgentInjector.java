package de.plushnikov.intellij.plugin.hack;

import com.sun.jna.Platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class AgentInjector {

  static {
    if (Platform.isMac()) {
      final String key = "jna.library.path", home = System.getProperty("java.home"), lib = home + separator + "lib", server = lib + separator + "server";
      System.setProperty(key, lib + pathSeparator + server + pathSeparator + System.getProperty(key, ""));
    }
  }

  static void inject(final String prefix, final Class<?> agent, final Class<?>... resources) throws IOException {
    JNI.Instrument.INSTANCE.attachAgent(generateAgentJar(prefix, agent, resources));
  }

  static String generateAgentJar(final String prefix, final Class<?> agent, final Class<?>... resources) throws IOException {
    final Path jarFile = Files.createTempFile(prefix + "-Agent-", ".jar");
    final Manifest manifest = new Manifest();
    final Attributes attributes = manifest.getMainAttributes();
    // Create manifest stating that agent is allowed to transform classes
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.put(new Attributes.Name("Agent-Class"), agent.getName());
    attributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
    attributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
    try (final JarOutputStream output = new JarOutputStream(Files.newOutputStream(jarFile, CREATE, WRITE), manifest)) {
      output.putNextEntry(new JarEntry(path(agent)));
      output.write(getBytesFromClass(agent));
      for (final Class<?> resource : resources) {
        output.putNextEntry(new JarEntry(path(resource)));
        output.write(getBytesFromClass(resource));
      }
    }
    return jarFile.toRealPath().toString();
  }

  private static String path(final Class<?> clazz) { return clazz.getName().replace('.', '/') + ".class"; }

  private static byte[] getBytesFromClass(final Class<?> clazz) throws IOException {
    try (final InputStream input = clazz.getClassLoader().getResourceAsStream(path(clazz))) {
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
