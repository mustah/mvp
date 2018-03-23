package com.elvaco.mvp.core.exception;

import java.util.List;

public class PredicateConstructionFailure extends RuntimeException {

  private static final long serialVersionUID = 4328086570851661809L;

  public PredicateConstructionFailure(
    String key,
    List<String> propertyValues,
    Exception exception
  ) {
    super(String.format(
      "Failed to construct filter '%s' for %s",
      key,
      propertyValues.size() == 1
        ? "value '" + propertyValues.get(0) + "'"
        : "values " + propertyValues
    ), exception);
  }
}
