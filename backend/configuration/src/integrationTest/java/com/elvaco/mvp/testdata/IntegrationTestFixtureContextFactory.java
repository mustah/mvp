package com.elvaco.mvp.testdata;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
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
      OrganisationEntity.builder()
        .id(contextUuid)
        .name(callSiteIdentifier + "-organisation")
        .slug(contextUuid.toString())
        .externalId(contextUuid.toString())
        .build()
    );

    User user = new UserBuilder()
      .name("integration-test-user")
      .email(contextUuid.toString() + "@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asUser()
      .build();
    users.create(user);

    User admin = new UserBuilder()
      .name("integration-test-admin")
      .email(contextUuid.toString() + "-admin@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asAdmin()
      .build();
    users.create(admin);

    User superAdmin = new UserBuilder()
      .name("integration-test-super-admin")
      .email(contextUuid.toString() + "-super-admin@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .id(randomUUID())
      .asSuperAdmin()
      .build();
    users.create(superAdmin);

    UUID contextUuid2 = randomUUID();
    OrganisationEntity organisation2 = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(contextUuid2)
        .name(callSiteIdentifier + "-organisation")
        .slug(contextUuid2.toString())
        .externalId(contextUuid2.toString())
        .build()
    );

    User user2 = new UserBuilder()
      .name("integration-test-user2")
      .email(contextUuid2.toString() + "@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
      .id(randomUUID())
      .asUser()
      .build();
    users.create(user2);

    User admin2 = new UserBuilder()
      .name("integration-test-admin2")
      .email(contextUuid2.toString() + "-admin2@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
      .id(randomUUID())
      .asAdmin()
      .build();
    users.create(admin2);

    User superAdmin2 = new UserBuilder()
      .name("integration-test-super-admin2")
      .email(contextUuid.toString() + "-super-admin2@test.com")
      .password("password")
      .language(Language.en)
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
      .id(randomUUID())
      .asSuperAdmin()
      .build();
    users.create(superAdmin2);


    return new IntegrationTestFixtureContext(
      organisation,
      user,
      admin,
      superAdmin,
      organisation2,
      user2,
      admin2,
      superAdmin2
    );
  }

  @Transactional
  public void destroy(IntegrationTestFixtureContext context) {
    try {
      organisationJpaRepository.deleteById(context.organisationEntity.id);
    } catch (EmptyResultDataAccessException ignore) {
      // The test case probably removed it already
    }
    try {
      organisationJpaRepository.deleteById(context.organisationEntity2.id);
    } catch (EmptyResultDataAccessException ignore) {
      // The test case probably removed it already
    }
  }
}
