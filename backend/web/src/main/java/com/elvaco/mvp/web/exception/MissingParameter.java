package com.elvaco.mvp.web.exception;

import com.elvaco.mvp.core.spi.data.RequestParameter;

public class MissingParameter extends RuntimeException {

  public MissingParameter(RequestParameter parameter) {
    super(String.format("Missing '%s' parameter.", parameter));
  }
}
