package com.elvaco.mvp.core.exception;

public class InvalidMeterDefinition extends RuntimeException {
  private static final long serialVersionUID = -2155802966445665142L;

  public InvalidMeterDefinition(String message) {
    super(message);
  }
}
