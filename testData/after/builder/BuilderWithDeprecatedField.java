public class BuilderWithDeprecatedField {
  private String bar;
  @Deprecated
  private String foo;

  @java.beans.ConstructorProperties({"bar", "foo"})
  BuilderWithDeprecatedField(String bar, String foo) {
    this.bar = bar;
    this.foo = foo;
  }

  public static BuilderWithDeprecatedFieldBuilder builder() {
    return new BuilderWithDeprecatedFieldBuilder();
  }

  public static class BuilderWithDeprecatedFieldBuilder {
    private String bar;
    private String foo;

    BuilderWithDeprecatedFieldBuilder() {
    }

    public BuilderWithDeprecatedFieldBuilder bar(String bar) {
      this.bar = bar;
      return this;
    }

    @Deprecated
    public BuilderWithDeprecatedFieldBuilder foo(String foo) {
      this.foo = foo;
      return this;
    }

    public BuilderWithDeprecatedField build() {
      return new BuilderWithDeprecatedField(bar, foo);
    }

    public String toString() {
      return "BuilderWithDeprecatedField.BuilderWithDeprecatedFieldBuilder(bar=" + this.bar + ", foo=" + this.foo + ")";
    }
  }
}
