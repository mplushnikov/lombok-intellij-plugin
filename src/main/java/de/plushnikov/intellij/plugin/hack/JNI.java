package de.plushnikov.intellij.plugin.hack;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface JNI extends Library {

  JNI INSTANCE = Native.load("jvm", JNI.class);

  /* JNI Result code */
  int
    // @formatter:off
            JNI_OK        = 0,      /* success */
            JNI_ERR       = -1,     /* unknown error */
            JNI_EDETACHED = -2,     /* thread detached from the VM */
            JNI_EVERSION  = -3,     /* JNI version error */
            JNI_ENOMEM    = -4,     /* not enough memory */
            JNI_EEXIST    = -5,     /* VM already created */
            JNI_EINVAL    = -6;     /* invalid arguments */
            // @formatter:on

  /* JNI VersionInfo */
  int
    // @formatter:off
            JNI_VERSION_1_1 = 0x00010001,
            JNI_VERSION_1_2 = 0x00010002,
            JNI_VERSION_1_4 = 0x00010004,
            JNI_VERSION_1_6 = 0x00010006,
            JNI_VERSION_1_8 = 0x00010008,
            JNI_VERSION_9   = 0x00090000,
            JNI_VERSION_10  = 0x000a0000;
            // @formatter:on

  @Structure.FieldOrder("reserved")
  class NativeInterface extends Structure {

    public static class ByReference extends NativeInterface implements Structure.ByReference { }

    public static class ByValue extends NativeInterface implements Structure.ByValue { }

    public long reserved;

    public NativeInterface() { }

    public NativeInterface(final Pointer pointer) {
      super(pointer);
      autoRead();
    }

  }

  @Structure.FieldOrder("functions")
  class Env extends Structure {

    public static class ByReference extends NativeInterface implements Structure.ByReference { }

    public static class ByValue extends NativeInterface implements Structure.ByValue { }

    public @Nullable NativeInterface.ByReference functions;

    public Env() { }

    public Env(final Pointer pointer) {
      super(pointer);
      autoRead();
    }

  }

  @SuppressWarnings("ConstantConditions")
  @Structure.FieldOrder("functions")
  class JavaVM extends Structure {

    public static class ByReference extends JavaVM implements Structure.ByReference { }

    public static class ByValue extends JavaVM implements Structure.ByValue { }

    @Structure.FieldOrder({
      "reserved0",
      "reserved1",
      "reserved2",
      "DestroyJavaVM",
      "AttachCurrentThread",
      "DetachCurrentThread",
      "GetEnv",
      "AttachCurrentThreadAsDaemon"
    })
    public static class InvokeInterface extends Structure {

      public static class ByReference extends InvokeInterface implements Structure.ByReference { }

      public static class ByValue extends InvokeInterface implements Structure.ByValue { }

      public @Nullable Pointer reserved0, reserved1, reserved2;

      public interface DestroyJavaVM extends Callback {

        int invoke(Pointer p_javaVM);

      }

      public @Nullable DestroyJavaVM DestroyJavaVM;

      public interface AttachCurrentThread extends Callback {

        int invoke(Pointer p_javaVM, PointerByReference p_penv, Pointer p_args);

      }

      public @Nullable AttachCurrentThread AttachCurrentThread;

      public interface DetachCurrentThread extends Callback {

        int invoke(Pointer p_javaVM);

      }

      public @Nullable DetachCurrentThread DetachCurrentThread;

      public interface GetEnv extends Callback {

        int invoke(Pointer p_javaVM, PointerByReference p_penv, int version);

      }

      public @Nullable GetEnv GetEnv;

      public interface AttachCurrentThreadAsDaemon extends Callback {

        int invoke(Pointer p_javaVM, PointerByReference p_penv, Pointer p_args);

      }

      public @Nullable AttachCurrentThreadAsDaemon AttachCurrentThreadAsDaemon;

      public InvokeInterface() { }

      public InvokeInterface(final Pointer pointer) {
        super(pointer);
      }

    }

    public @Nullable InvokeInterface.ByReference functions;

    public JavaVM() { }

    public JavaVM(final Pointer pointer) {
      super(pointer);
      autoRead();
    }

    public void destroyJavaVM() throws LastErrorException {
      checkJNIError(functions.DestroyJavaVM.invoke(getPointer()));
    }

    public void attachCurrentThread(final PointerByReference p_penv, final Pointer p_args) throws LastErrorException {
      checkJNIError(functions.AttachCurrentThread.invoke(getPointer(), p_penv, p_args));
    }

    public void detachCurrentThread() throws LastErrorException {
      checkJNIError(functions.DetachCurrentThread.invoke(getPointer()));
    }

    public <T> T getEnv(final Function<Pointer, ? extends T> mapper, final int version) throws LastErrorException {
      final PointerByReference p_penv = new PointerByReference();
      checkJNIError(functions.GetEnv.invoke(getPointer(), p_penv, version));
      return mapper.apply(p_penv.getValue());
    }

    public void attachCurrentThreadAsDaemon(final PointerByReference p_penv, final Pointer p_args) throws LastErrorException {
      checkJNIError(functions.AttachCurrentThreadAsDaemon.invoke(getPointer(), p_penv, p_args));
    }

    public JNI.Env jniEnv(final int version) throws LastErrorException {
      return getEnv(JNI.Env::new, version);
    }

    public static JavaVM contextVM() { return new JavaVM(INSTANCE.contextVM()); }

  }

  interface Instrument extends Library {

    Instrument INSTANCE = Native.load("instrument", Instrument.class);

    int Agent_OnAttach(Pointer p_vm, String path, @Nullable Pointer p_reserved);

    default void attachAgent(final String path, final @Nullable Pointer p_reserved) {
      checkJNIError(Agent_OnAttach(JNI.INSTANCE.contextVM(), path, p_reserved));
    }

    default void attachAgent(final String path) { attachAgent(path, null); }

  }

  static void checkJNIError(final int jniReturnCode) throws LastErrorException {
    if (jniReturnCode != JNI_OK)
      throw new LastErrorException(jniReturnCode);
  }

  int JNI_GetDefaultJavaVMInitArgs(Pointer p_args);

  int JNI_CreateJavaVM(PointerByReference p_vms, PointerByReference p_penv, Pointer p_args);

  int JNI_GetCreatedJavaVMs(PointerByReference p_vms, int count, IntByReference p_found);

  default Pointer contextVM() throws LastErrorException {
    final PointerByReference p_vms = new PointerByReference();
    final IntByReference p_found = new IntByReference();
    checkJNIError(JNI.INSTANCE.JNI_GetCreatedJavaVMs(p_vms, 1, p_found));
    return p_vms.getValue();
  }

}
