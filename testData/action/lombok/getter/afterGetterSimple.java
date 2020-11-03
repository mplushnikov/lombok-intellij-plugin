import lombok.Getter;

@Getter
class Test {
  private float b;
  private double c;
  private String d;

  private int wrongReturnType;
  private int wrongField;
  private int fieldWithoutGetter;
  private int sideEffect;
  private int javadoc;
  private int comment;

  public long getWrongReturnType() {
    return wrongReturnType;
  }

  public int getWrongField() {
    return wrongReturnType;
  }

  public int getSideEffect() {
    System.out.println("side-effect");
    return sideEffect;
  }
}
