package com.elvaco.mvp.exception;

public class UserNotFound extends RuntimeException {

  public UserNotFound(String username) {
    super("Unable to find user '" + username + "'");
  }
}
