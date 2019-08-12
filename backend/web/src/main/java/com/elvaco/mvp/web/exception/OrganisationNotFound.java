package com.elvaco.mvp.web.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OrganisationNotFound extends RuntimeException {

  private static final long serialVersionUID = -3612670568377718769L;

  public OrganisationNotFound(UUID id) {
    super("Unable to find organisation with ID '" + id.toString() + "'");
  }

  public OrganisationNotFound(String slug) {
    super("Unable to find organisation with slug '" + slug + "'");
  }
}
