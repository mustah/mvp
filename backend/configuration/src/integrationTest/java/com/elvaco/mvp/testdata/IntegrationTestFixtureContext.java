package com.elvaco.mvp.testdata;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;

public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;

  IntegrationTestFixtureContext(OrganisationEntity organisation, User user) {
    this.organisationEntity = organisation;
    this.user = user;
  }
}
