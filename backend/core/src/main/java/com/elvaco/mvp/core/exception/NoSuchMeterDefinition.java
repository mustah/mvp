package com.elvaco.mvp.core.exception;

import com.elvaco.mvp.core.domainmodels.Medium;

public class NoSuchMeterDefinition extends RuntimeException {
  private static final long serialVersionUID = 6892425875476443797L;

  public NoSuchMeterDefinition(Medium medium) {
    super(String.format("System meter definition for medium '%s' does not exist", medium));
  }
}
