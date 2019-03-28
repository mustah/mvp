package com.elvaco.mvp.web.exception;

public class InvalidParameter extends RuntimeException {

  private static final long serialVersionUID = -3224766475563493695L;

  public InvalidParameter(String parameterName) {
    super(String.format("Parameter '%s' is invalid", parameterName));
  }
}
