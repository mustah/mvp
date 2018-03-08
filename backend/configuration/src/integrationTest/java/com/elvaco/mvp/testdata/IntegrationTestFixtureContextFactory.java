package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class IntegrationTestFixtureContextFactory {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final Users users;
  private final OrganisationMapper organisationMapper;

  @Autowired
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
      .id(contextUuid)
      .asUser()
      .build();

    return new IntegrationTestFixtureContext(organisation, user);
  }

  public void destroy(IntegrationTestFixtureContext context) {
    organisationJpaRepository.delete(context.organisationEntity.id);
  }
}
