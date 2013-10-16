package de.plushnikov.log;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Log4j2Class {
    private int intProperty;

    private float floatProperty;

    private String stringProperty;

    public void doSomething() {
        Log4j2Class.log.warn("Warning message text");
        Log4j2Class.log.error("Error message text");
    }

    public static void main(String[] args) {
        new Log4jClass().doSomething();
    }
}
