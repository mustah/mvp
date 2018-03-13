package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoPhysicalMetersException extends RuntimeException {

  private static final long serialVersionUID = -6787495722404547820L;
}
