package com.elvaco.mvp.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidToken extends AuthenticationException {

  private static final long serialVersionUID = 4678853779814063415L;

  public InvalidToken() {
    super("Token missing or invalid");
  }
}
