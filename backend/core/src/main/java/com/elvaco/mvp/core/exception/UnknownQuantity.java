package com.elvaco.mvp.core.exception;

public class UnknownQuantity extends RuntimeException {

  private static final long serialVersionUID = 8115544597677652147L;

  public UnknownQuantity(String quantity) {
    super(String.format("Unknown quantity: %s", quantity));
  }
}
