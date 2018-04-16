package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class UserSelection {
  public final UUID id;
  public final UUID ownerUserId;
  public final String name;
  public final JsonNode data;
  public final UUID organisationId;

  public UserSelection withUserId(UUID userId) {
    return new UserSelection(
      id,
      userId,
      name,
      data,
      organisationId
    );
  }

  public UserSelection withOrganisationId(UUID newOrganisationId) {
    return new UserSelection(
      id,
      ownerUserId,
      name,
      data,
      newOrganisationId
    );
  }
}
