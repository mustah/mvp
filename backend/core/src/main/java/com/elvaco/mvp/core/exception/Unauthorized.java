package com.elvaco.mvp.core.exception;

public class Unauthorized extends RuntimeException {

  private static final long serialVersionUID = 2981700686816129657L;

  public Unauthorized(String message) {
    super(message);
  }
}
