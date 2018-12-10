package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntegrationTestFixtureContext {

  public final OrganisationEntity organisationEntity;
  public final User user;
  public final User admin;
  public final User superAdmin;

  public final OrganisationEntity organisationEntity2;
  public final User user2;
  public final User admin2;
  public final User superAdmin2;

  public Organisation organisation() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity);
  }

  public Organisation organisation2() {
    return OrganisationEntityMapper.toDomainModel(organisationEntity2);
  }

  public UUID organisationId() {
    return organisation().id;
  }

  public UUID organisationId2() {
    return organisation2().id;
  }
}
