package de.plushnikov.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class Slf4jClass {
  private final Logger logger = LoggerFactory.getLogger(Slf4jClass.class);

  private int intProperty;

  private float floatProperty;

  private String stringProperty;

  public void doSomething() {
    log.info("Information message text");
    log.getName();
  }
}
