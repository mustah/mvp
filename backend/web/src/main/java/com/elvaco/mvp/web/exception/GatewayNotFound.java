package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GatewayNotFound extends RuntimeException {
  private static final long serialVersionUID = -1247102156990699323L;

  public GatewayNotFound(String id) {
    super("Unable to find gateway with ID '" + id + "'");
  }
}
