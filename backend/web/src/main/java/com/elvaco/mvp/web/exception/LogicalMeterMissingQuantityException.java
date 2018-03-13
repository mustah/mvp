package com.elvaco.mvp.web.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LogicalMeterMissingQuantityException extends RuntimeException {

  private static final long serialVersionUID = 3138787487426728173L;

  public LogicalMeterMissingQuantityException(UUID logicalMeterId, String quantity) {
    super("Meter " + logicalMeterId + " does not have the quantity '" + quantity + "' defined");
  }
}
