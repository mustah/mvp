package com.elvaco.mvp.web.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserSelectionNotFound extends RuntimeException {
  private static final long serialVersionUID = -1247102156990699323L;

  public UserSelectionNotFound(UUID id) {
    super("Unable to find user selection with ID '" + id + "'");
  }
}
