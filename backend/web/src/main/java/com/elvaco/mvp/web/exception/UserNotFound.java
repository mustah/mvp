package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFound extends RuntimeException {

  private static final long serialVersionUID = 5650238125513553630L;

  private UserNotFound(String message) {
    super(message);
  }

  public static UserNotFound withId(String id) {
    return new UserNotFound("Unable to find user with ID '" + id + "'");
  }

  public static UserNotFound withUsername(String username) {
    return new UserNotFound("Unable to find user '" + username + "'");
  }
}
