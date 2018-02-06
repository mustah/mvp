package com.elvaco.mvp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OrganisationNotFound extends RuntimeException {

  public OrganisationNotFound(Long id) {
    super("Unable to find organisation with ID '" + id + "'");
  }
}
