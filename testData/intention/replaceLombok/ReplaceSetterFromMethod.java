public class ReplaceSetterFromMethod {
  private int field;

  public int set<caret>Field(int field) {
    this.field = field;
  }
}
