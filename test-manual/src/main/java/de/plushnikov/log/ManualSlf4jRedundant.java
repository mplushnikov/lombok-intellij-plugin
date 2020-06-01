package de.plushnikov.log;

import org.slf4j.Logger;

public class ManualSlf4jRedundant {
  private static final Logger log1123 = org.slf4j.LoggerFactory.getLogger(de.plushnikov.log.ManualSlf4jRedundant.class);

  public void doSomething() {
    log1123.info("Information message text");
    log1123.getName();
  }
}
