package com.elvaco.mvp.core.domainmodels;

import java.util.stream.Stream;

public enum StatusType {

  OK("ok"),
  INFO("info"),
  ACTIVE("active"),
  CRITICAL("critical"),
  WARNING("warning"),
  UNKNOWN("unknown"),
  MAINTENANCE_SCHEDULED("maintenance scheduled"),
  ERROR("error");

  public final String name;

  StatusType(String name) {
    this.name = name;
  }

  public static StatusType from(String status) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(status))
      .findFirst().orElse(UNKNOWN);
  }
}
