package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.testing.fixture.UserBuilder;

class IntegrationTestFixtureContextFactory {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final Users users;
  private final OrganisationMapper organisationMapper;

  IntegrationTestFixtureContextFactory(
    OrganisationJpaRepository organisationJpaRepository,
    Users users
  ) {
    this.organisationJpaRepository = organisationJpaRepository;
    this.organisationMapper = new OrganisationMapper();
    this.users = users;
  }

  public IntegrationTestFixtureContext create() {
    UUID contextUuid = UUID.randomUUID();
    OrganisationEntity organisation = organisationJpaRepository.save(
      new OrganisationEntity(
        contextUuid,
        contextUuid.toString().substring(0, 10) + "-organisation",
        contextUuid.toString()
      )
    );

    User user = new UserBuilder().name("integration-test-user")
      .email(contextUuid.toString() + "@test.com")
      .password("password")
      .organisation(organisationMapper.toDomainModel(organisation))
      .id(UUID.randomUUID())
      .asUser()
      .build();

    users.create(user);

    User admin = new UserBuilder().name("integration-test-admin")
      .email(contextUuid.toString() + "-admin@test.com")
      .password("password")
      .organisation(organisationMapper.toDomainModel(organisation))
      .id(UUID.randomUUID())
      .asAdmin()
      .build();
    users.create(admin);
    return new IntegrationTestFixtureContext(organisation, organisationMapper, user, admin);
  }

  public void destroy(IntegrationTestFixtureContext context) {
    organisationJpaRepository.delete(context.organisationEntity.id);
  }
}
