class Test {
  private String name;
  private Integer age;
  private char sex;


  public <T> T toBean(Class<T> clazz) {
    return JsonUtils.convert(this, clazz);
  }

  public static <T> Test fromBean(T param) {
    return (Test) JsonUtils.convert(param, Test.class);
  }

  public String toJson() {
    return JsonUtils.beanToJson(this);
  }

  public static Test fromJson(String jsonStr) {
    return (Test) JsonUtils.jsonToBean(jsonStr, Test.class);
  }
}
