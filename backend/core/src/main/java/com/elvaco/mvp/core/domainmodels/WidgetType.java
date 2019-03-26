package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;

public enum WidgetType {
  MAP,
  COLLECTION,
  ;

  public static Optional<WidgetType> from(String type) {
    try {
      return Optional.of(valueOf(type));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}


