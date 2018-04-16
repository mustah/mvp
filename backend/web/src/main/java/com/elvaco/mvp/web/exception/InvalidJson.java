package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidJson extends RuntimeException {

  private static final long serialVersionUID = -31387817426722273L;

  public InvalidJson(String json) {
    super("String is not valid JSON: " + json);
  }
}
