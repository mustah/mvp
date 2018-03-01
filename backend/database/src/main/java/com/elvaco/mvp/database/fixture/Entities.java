package com.elvaco.mvp.database.fixture;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import static java.util.UUID.randomUUID;

public final class Entities {

  public static final OrganisationEntity ELVACO_ENTITY =
    new OrganisationEntity(
      randomUUID(),
      "Elvaco",
      "elvaco"
    );

  private Entities() { }
}
