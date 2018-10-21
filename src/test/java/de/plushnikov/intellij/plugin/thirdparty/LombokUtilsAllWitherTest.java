package de.plushnikov.intellij.plugin.thirdparty;

import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LombokUtilsAllWitherTest {

  private final List<String> lombokResult = new ArrayList<String>();
  private final List<String> result = new ArrayList<String>();

  private void makeResults(String fieldName, boolean isBoolean) {
    lombokResult.clear();
    result.clear();

    final AccessorsInfo accessorsInfo = AccessorsInfo.EMPTY;
    lombokResult.addAll(LombokHandlerUtil.toAllWitherNames(accessorsInfo, fieldName, isBoolean));
    result.addAll(LombokUtils.toAllWitherNames(accessorsInfo, fieldName, isBoolean));

    assertThat(result, is(lombokResult));
  }

  @Test
  public void testToAllWitherNames_NonBoolean() {
    makeResults("myField", false);

    assertThat(result, is(Arrays.asList("withMyField")));
  }

  @Test
  public void testToAllWitherNames_NonBoolean_Uppercase() {
    makeResults("myField", false);

    assertThat(result, is(Arrays.asList("withMyField")));
  }

  @Test
  public void testToAllWitherNames_NonBoolean_Uppercase_Multiple() {
    makeResults("MYField", false);

    assertThat(result, is(Arrays.asList("withMYField")));
  }

  @Test
  public void testToAllWitherNames_Boolean() {
    makeResults("myField", true);

    assertThat(result, is(Arrays.asList("withMyField")));
  }

  @Test
  public void testToAllWitherNames_Boolean_Uppercase() {
    makeResults("MyField", true);

    assertThat(result, is(Arrays.asList("withMyField")));
  }

  @Test
  public void testToAllWitherNames_Boolean_is_Lowercase() {
    makeResults("ismyField", true);

    assertThat(result, is(Arrays.asList("withIsmyField")));
  }

  @Test
  public void testToAllWitherNames_Boolean_is_Uppercase() {
    makeResults("isMyField", true);

    assertThat(result, is(Arrays.asList("withIsMyField", "withMyField")));
  }

  @Test
  public void testToAllWitherNames_Boolean_IS() {
    makeResults("ISmyField", true);

    assertThat(result, is(Arrays.asList("withISmyField")));
  }

}
