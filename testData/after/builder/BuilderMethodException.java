// Generated by delombok at Wed Oct 02 19:12:43 GMT 2019

class Exception {
}

public class BuilderMethodException {
  private static void foo(int i) throws Exception {
    System.out.println("sss");
  }

  public static void main(String[] args) {
    try {
      builder().i(2).build();
    } catch (Exception ignore) {
    }
  }


  @java.lang.SuppressWarnings("all")
  public static class VoidBuilder {
    @java.lang.SuppressWarnings("all")
    private int i;

    @java.lang.SuppressWarnings("all")
    VoidBuilder() {
    }

    @java.lang.SuppressWarnings("all")
    public VoidBuilder i(final int i) {
      this.i = i;
      return this;
    }

    @java.lang.SuppressWarnings("all")
    public void build() throws Exception {
      BuilderMethodException.foo(this.i);
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
      return "BuilderMethodException.VoidBuilder(i=" + this.i + ")";
    }
  }

  @java.lang.SuppressWarnings("all")
  public static VoidBuilder builder() {
    return new VoidBuilder();
  }
}
