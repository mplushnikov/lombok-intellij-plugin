@lombok.Builder
public class BuilderWithDeprecatedField {
  private String bar;

  @Deprecated
  private String foo;
}
