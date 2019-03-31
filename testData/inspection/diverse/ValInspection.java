import lombok.val;

public class ValInspection {

  public void test() {
    val a = 1;

    val b = "a2";

    val c = new int[]{1};

    val d = System.getProperty("sss");

    // 'val' is not allowed in old-style for loops
    for (<error descr = "'val' is not allowed in old-style for loops"> val i = 0;</error> i < 10; i++){
      val j = 2;
    }

  }
}
