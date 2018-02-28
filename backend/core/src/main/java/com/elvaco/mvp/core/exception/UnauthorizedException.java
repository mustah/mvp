package com.elvaco.mvp.core.exception;

public class UnauthorizedException extends RuntimeException {

  private static final long serialVersionUID = 2981700686816129657L;

  public UnauthorizedException(String message) {
    super(message);
  }
}
