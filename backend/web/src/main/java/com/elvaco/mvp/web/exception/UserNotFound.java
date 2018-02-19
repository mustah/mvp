package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFound extends RuntimeException {

  private static final long serialVersionUID = 5650238125513553630L;

  public UserNotFound(Long id) {
    super("Unable to find user with ID '" + id + "'");
  }

  public UserNotFound(String username) {
    super("Unable to find user '" + username + "'");
  }
}
