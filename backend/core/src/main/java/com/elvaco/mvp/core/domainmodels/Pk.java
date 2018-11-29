package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Pk implements PrimaryKey {

  private final UUID id;
  private final UUID organisationId;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getOrganisationId() {
    return organisationId;
  }
}
