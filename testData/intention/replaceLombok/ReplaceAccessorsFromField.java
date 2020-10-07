public class ReplaceAccessorsFromField {
  private int fi<caret>eld;

  public int getField() {
    return field;
  }

  public int setField(int field) {
    this.field = field;
  }
}
