package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class QuantityNotFound extends RuntimeException {

  private static final long serialVersionUID = 3138787487426728173L;

  public QuantityNotFound(String quantity) {
    super("Could not find any meters exposing quantity '" + quantity + "'.");
  }
}
