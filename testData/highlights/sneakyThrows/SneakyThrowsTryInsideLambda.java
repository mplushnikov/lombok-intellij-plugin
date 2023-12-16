import lombok.SneakyThrows;

public class SneakyThrowsTryInsideLambda {
  @SneakyThrows
  public static void m() {
    Runnable r = () -> {
      try (Reader reader = new <error descr = "Unhandled exception: java.io.FileNotFoundException" > FileReader </error>("")){
      }
    }
  }

  // everything is ok here
  @SneakyThrows
  void m2() {
    try (Reader reader = new FileReader("")) {}
  }

}