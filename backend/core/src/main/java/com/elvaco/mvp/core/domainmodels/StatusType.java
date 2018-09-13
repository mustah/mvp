package com.elvaco.mvp.core.domainmodels;

import java.util.stream.Stream;

public enum StatusType {

  OK("ok"),
  ERROR("error"),
  WARNING("warning"),
  UNKNOWN("unknown");

  public final String name;

  StatusType(String name) {
    this.name = name;
  }

  public boolean isReported() {
    return this != OK;
  }

  public static StatusType from(String status) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(status))
      .findFirst()
      .orElse(UNKNOWN);
  }
}
