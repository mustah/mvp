package com.elvaco.mvp.core.domainmodels;

import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public enum StatusType {

  OK("ok"),
  ERROR("error"),
  WARNING("warning"),
  UNKNOWN("unknown");

  private static final Map<String, StatusType> STATUS_ALIASES = Map.of("ErrorReported", ERROR);

  public final String name;

  StatusType(String name) {
    this.name = name;
  }

  public static StatusType from(@Nullable String status) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(status) || s.hasAlias(status))
      .findFirst()
      .orElse(UNKNOWN);
  }

  public static boolean isReported(String status) {
    StatusType type = StatusType.from(status);
    return type.isNotUnknown() && type.isNotOk();
  }

  public boolean isNotOk() {
    return this != OK;
  }

  public boolean isNotUnknown() {
    return this != UNKNOWN;
  }

  private boolean hasAlias(String status) {
    return this == STATUS_ALIASES.get(status);
  }
}
