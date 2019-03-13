package com.elvaco.mvp.web.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WidgetNotFound extends RuntimeException {

  private static final long serialVersionUID = 1795590597758803267L;

  public WidgetNotFound(UUID id) {
    super("Unable to find widget with id '" + id + "'");
  }
}
