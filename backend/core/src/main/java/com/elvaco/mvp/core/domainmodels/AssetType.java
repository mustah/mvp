package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AssetType {
  LOGIN_BACKGROUND("login_background"),
  LOGIN_LOGOTYPE("login_logotype"),
  LOGOTYPE("logotype");

  public final String name;

  public static Optional<AssetType> fromString(String assetType) {
    return Stream.of(values())
      .filter(s -> s.name.equalsIgnoreCase(assetType))
      .findAny();
  }

  public String toString() {
    return name;
  }
}
