package de.plushnikov.tostring;

import lombok.ToString;

import java.util.Date;

@ToString(doNotUseGetters = true, callSuper = false, of = {"floatProperty"})
public class ClassToString extends Date {
    private int intProperty;

    private float floatProperty;

    private static String stringProperty;
}
