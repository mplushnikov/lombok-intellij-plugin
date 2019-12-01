public class Issue440Prior183 {
  private Bar bar;

  @lombok.Getter(lazy = true)
  private final String barString = bar.toString();

  private Car car;

  private final String carString = car.<warning descr="Method invocation 'toString' may produce 'java.lang.NullPointerException'">toString</warning>();

  public Issue440Prior183(Bar bar) {
    this.bar = bar;
  }

  private static class Bar {

  }

  private static class Car {

  }
}
