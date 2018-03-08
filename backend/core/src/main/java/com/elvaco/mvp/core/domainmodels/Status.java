package com.elvaco.mvp.core.domainmodels;

import java.util.stream.Stream;

public enum Status {

  OK("ok"),
  INFO("info"),
  ACTIVE("active"),
  CRITICAL("critical"),
  WARNING("warning"),
  UNKNOWN("unknown"),
  MAINTENANCE_SCHEDULED("maintenance scheduled");

  private final String name;

  Status(String name) {
    this.name = name;
  }

  public static Status from(String status) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(status))
      .findFirst().orElse(UNKNOWN);
  }
}
