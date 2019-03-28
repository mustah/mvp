package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidWidgetType extends RuntimeException {
  private static final long serialVersionUID = -2178814571575647382L;

  public InvalidWidgetType(String type) {
    super("Invalid widget type: " + type);
  }
}
