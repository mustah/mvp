package com.elvaco.mvp.web.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DashboardNotFound extends RuntimeException {

  private static final long serialVersionUID = 8696393837580761292L;

  public DashboardNotFound(UUID id) {
    super("Unable to find dashboard with id '" + id + "'");
  }
}
