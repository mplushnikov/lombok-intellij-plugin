package de.plushnikov.inspection;

import java.util.Date;

public class Issue732 {
  private int someInt;
  private String someString;
  private Date someDate;

  public int getSomeInt() {
    return someInt + 1;
  }

  public void setSomeInt(int someInt) {
    this.someInt = someInt;
  }

  public String getSomeString() {
    return someString;
  }

  public void setSomeString(String someString) {
    this.someString
      =
      someString;
  }

  public Date getSomeDate() {
    return someDate;
  }

  public void setSomeDate(Date someDate) {
    this.someDate = someDate;
  }
}
