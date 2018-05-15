package com.elvaco.mvp.core.exception;

import java.util.UUID;

public class NoPhysicalMeters extends RuntimeException {

  private static final long serialVersionUID = -6787495722404547820L;

  public NoPhysicalMeters(UUID logicalMeterId, String logicalMeterExternalId) {
    super(String.format(
      "No physical meters connected to logical meter '%s' (%s)",
      logicalMeterId,
      logicalMeterExternalId
    ));
  }
}
