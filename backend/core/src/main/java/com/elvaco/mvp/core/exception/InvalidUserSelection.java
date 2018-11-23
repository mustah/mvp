package com.elvaco.mvp.core.exception;

import java.util.UUID;

public class InvalidUserSelection extends RuntimeException {

  private InvalidUserSelection(String message) {
    super(message);
  }

  public static InvalidUserSelection misconfiguredParentOrganisationSelection(
    UUID parentOrganisation,
    UUID subOrganisation
  ) {
    return new InvalidUserSelection(String.format(
      "Selection is misconfigured for parent organisation (%s) and sub-organisation (%s)",
      parentOrganisation.toString(),
      subOrganisation.toString()
    ));
  }
}
