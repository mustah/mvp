package com.elvaco.mvp.web.exception;

public class InvalidParameter extends RuntimeException {

  public InvalidParameter(String parameterName) {
    super(String.format("Parameter '%s' is invalid", parameterName));
  }
}
