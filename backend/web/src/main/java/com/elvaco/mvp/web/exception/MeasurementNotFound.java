package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class MeasurementNotFound extends RuntimeException {
  private static final long serialVersionUID = 1463691787235502138L;

  public MeasurementNotFound(Long id) {
    super("Unable to find measurement with ID '" + id + "'");
  }
}
