package com.elvaco.mvp.core.domainmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;

public enum StatusType {

  OK("ok"),
  ERROR("error"),
  WARNING("warning"),
  UNKNOWN("unknown");

  private static final Map<String, StatusType> STATUS_ALIASES = statusTypeAliasMap();
  public final String name;

  StatusType(String name) {
    this.name = name;
  }

  public static StatusType from(String status) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(status) || s.hasAlias(status))
      .findFirst()
      .orElse(UNKNOWN);
  }

  private boolean hasAlias(String status) {
    return equals(STATUS_ALIASES.get(status));
  }

  public boolean isReported() {
    return this != OK;
  }

  private static Map<String, StatusType> statusTypeAliasMap() {
    Map<String, StatusType> map = new HashMap<>();
    map.put("ErrorReported", ERROR);
    return unmodifiableMap(map);
  }
}
