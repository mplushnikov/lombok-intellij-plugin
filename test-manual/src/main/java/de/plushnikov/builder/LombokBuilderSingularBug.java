package de.plushnikov.builder;

import lombok.Builder;
import lombok.Singular;

import java.util.HashSet;
import java.util.Set;

public class LombokBuilderSingularBug {
    final Set<String> strings;

    @Builder
    private LombokBuilderSingularBug(@Singular Set<String> strings) {
        this.strings = strings;
    }

    public void test() {
        LombokBuilderSingularBug.builder().string("A").clearStrings().string("");


        LombokBuilderSingularBug.builder().strings(new HashSet<>());
    }
}
