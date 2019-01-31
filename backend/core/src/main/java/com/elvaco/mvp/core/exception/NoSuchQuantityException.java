package com.elvaco.mvp.core.exception;

public class NoSuchQuantityException extends RuntimeException {
  private static final long serialVersionUID = 7848653426153938675L;

  public NoSuchQuantityException(String quantityName) {
    super(String.format("Quantity '%s' does not exist", quantityName));
  }
}
