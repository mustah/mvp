package com.elvaco.mvp.core.exception;

public class EmailAddressAlreadyExists extends RuntimeException {

  private static final long serialVersionUID = -6843766539862996646L;

  public EmailAddressAlreadyExists() {
    super("Email address already exists");
  }
}
