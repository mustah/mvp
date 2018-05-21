package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;

public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;
  public final User admin;
  public final User superAdmin;

  public final OrganisationEntity organisationEntity2;
  public final User user2;
  public final User admin2;
  public final User superAdmin2;

  IntegrationTestFixtureContext(
    OrganisationEntity organisation,
    User user,
    User admin,
    User superAdmin,
    OrganisationEntity organisation2,
    User user2,
    User admin2,
    User superAdmin2

  ) {
    this.organisationEntity = organisation;
    this.user = user;
    this.admin = admin;
    this.superAdmin = superAdmin;
    this.organisationEntity2 = organisation2;
    this.user2 = user2;
    this.admin2 = admin2;
    this.superAdmin2 = superAdmin2;

  }

  public Organisation organisation() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity);
  }

  public Organisation organisation2() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity2);
  }

  public UUID getOrganisationId() {
    return organisation().id;
  }

  public UUID getOrganisationId2() {
    return organisation2().id;
  }
}
