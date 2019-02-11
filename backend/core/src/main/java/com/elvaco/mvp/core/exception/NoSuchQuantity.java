package com.elvaco.mvp.core.exception;

public class NoSuchQuantity extends RuntimeException {
  private static final long serialVersionUID = 7848653426153938675L;

  public NoSuchQuantity(String quantityName) {
    super(String.format("Quantity '%s' does not exist", quantityName));
  }
}
