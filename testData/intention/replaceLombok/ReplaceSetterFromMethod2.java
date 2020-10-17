public class ReplaceSetterFromMethod2 {
  private int field;

  public int getField() {
    return field;
  }

  public int set<caret>Field(int field) {
    this.field = field;
  }
}
