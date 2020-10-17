public class ReplaceGetterFromField {
  private int fi<caret>eld;

  public int getField() {
    System.out.println("some stub");
    return 0;
  }

  public int setField(int field) {
    System.out.println("Additional monitoring");
    this.field = field;
  }
}
