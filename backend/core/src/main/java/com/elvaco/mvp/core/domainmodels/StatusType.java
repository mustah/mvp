package com.elvaco.mvp.core.domainmodels;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public enum StatusType {

  ERROR("error", "ErrorReported"),
  OK("ok"),
  WARNING("warning"),
  UNKNOWN("unknown");

  public final String name;

  @Nullable
  private final String alias;

  StatusType(String name, @Nullable String alias) {
    this.name = name;
    this.alias = alias;
  }

  StatusType(String name) {
    this(name, null);
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
    return Objects.equals(status, alias);
  }
}
