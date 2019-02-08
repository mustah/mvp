package com.elvaco.mvp.core.exception;

public class NoSuchMedium extends RuntimeException {
  private static final long serialVersionUID = 4702578615304649367L;

  public NoSuchMedium(String mediumName) {
    super(String.format("Medium '%s' does not exist", mediumName));
  }
}
