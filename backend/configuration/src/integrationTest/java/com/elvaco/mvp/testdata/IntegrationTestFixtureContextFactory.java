package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;

class IntegrationTestFixtureContextFactory {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final Users users;

  IntegrationTestFixtureContextFactory(
    OrganisationJpaRepository organisationJpaRepository,
    Users users
  ) {
    this.organisationJpaRepository = organisationJpaRepository;
    this.users = users;
  }

  @Transactional
  public IntegrationTestFixtureContext create(String callSiteIdentifier) {
    UUID contextUuid = randomUUID();
    OrganisationEntity organisation = organisationJpaRepository.save(
      new OrganisationEntity(
        contextUuid,
        callSiteIdentifier + "-organisation",
        contextUuid.toString(),
        contextUuid.toString()
      )
    );

    User user = new UserBuilder()
      .name("integration-test-user")
      .email(contextUuid.toString() + "@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asUser()
      .build();
    users.create(user);

    User admin = new UserBuilder()
      .name("integration-test-admin")
      .email(contextUuid.toString() + "-admin@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asAdmin()
      .build();
    users.create(admin);

    User superAdmin = new UserBuilder()
      .name("integration-test-super-admin")
      .email(contextUuid.toString() + "-super-admin@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asSuperAdmin()
      .build();
    users.create(superAdmin);

    return new IntegrationTestFixtureContext(
      organisation,
      user,
      admin,
      superAdmin
    );
  }

  @Transactional
  public void destroy(IntegrationTestFixtureContext context) {
    try {
      organisationJpaRepository.delete(context.organisationEntity.id);
    } catch (EmptyResultDataAccessException ignore) {
      // The test case probably removed it already
    }
  }
}
