package com.elvaco.mvp.fixture;

import com.elvaco.mvp.entity.user.OrganisationEntity;

public final class Entities {

  public static final OrganisationEntity ELVACO_ENTITY =
    new OrganisationEntity(
      1L,
      "Elvaco",
      "elvaco"
    );

  public static final OrganisationEntity WAYNE_INDUSTRIES_ENTITY =
    new OrganisationEntity(
      2L,
      "Wayne Industries",
      "wayne-industries"
    );

  private Entities() { }
}
