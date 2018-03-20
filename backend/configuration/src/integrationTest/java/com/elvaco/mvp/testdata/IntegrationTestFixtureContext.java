package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;

public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;
  public final User admin;
  private final OrganisationMapper organisationMapper;

  IntegrationTestFixtureContext(
    OrganisationEntity organisation,
    OrganisationMapper organisationMapper,
    User user,
    User admin
  ) {
    this.organisationMapper = organisationMapper;
    this.organisationEntity = organisation;
    this.user = user;
    this.admin = admin;
  }

  public Organisation organisation() {
    return organisationMapper.toDomainModel(organisationEntity);
  }

  public UUID getOrganisationId() {
    return organisation().id;
  }
}
