package de.plushnikov.builder.singularperformance;

import lombok.Builder;
import lombok.Singular;

import java.util.Collection;

@Builder
public class SingularPerformanceIssueDemo {
  @Singular
  private Collection<java.lang.String> number_0_strings;
  @Singular
  private Collection<java.lang.String> number_1_strings;
  @Singular
  private Collection<java.lang.String> number_2_strings;
  @Singular
  private Collection<java.lang.String> number_3_strings;
  @Singular
  private Collection<java.lang.String> number_4_strings;
  @Singular
  private Collection<java.lang.String> number_5_strings;
  @Singular
  private Collection<java.lang.String> number_6_strings;
  @Singular
  private Collection<java.lang.String> number_7_strings;
  @Singular
  private Collection<java.lang.String> number_8_strings;
  @Singular
  private Collection<java.lang.String> number_9_strings;
  @Singular
  private Collection<java.lang.String> number_10_strings;

  public static void main(String[] args) {
    for (int i = 0; i < 20; i++) {
      System.out.println("\t@Singular\n" + "\tprivate Collection<java.lang.String> number_" + i + "_strings;");
//      System.out.println("\tprivate Collection<java.lang.String> number_" + i + "_strings;");
    }
  }
}
