package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MeterNotFound extends RuntimeException {
  private static final long serialVersionUID = 7182504126222676279L;

  public MeterNotFound(String id) {
    super("Unable to find meter with ID '" + id + "'");
  }
}
