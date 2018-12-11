package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.MissingMeasurements;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MissingMeasurementUseCases {

  private final AuthenticatedUser currentUser;
  private final MissingMeasurements missingMeasurements;

  public boolean refreshAsUser() {
    if (currentUser.isSuperAdmin()) {
      return missingMeasurements.refresh();
    } else {
      throw new Unauthorized("User is not authorized to refresh missing measurements");
    }
  }

  public void refreshAsSystem() {
    missingMeasurements.refresh();
  }
}
