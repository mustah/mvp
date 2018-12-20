package com.elvaco.mvp.testdata;

import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
class IntegrationTestFixtureContextFactory {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final Users users;
  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final MeterAlarmLogs meterAlarmLogs;
  private final Measurements measurements;
  private final Gateways gateways;

  @Transactional
  public IntegrationTestFixtureContext create(String callSiteIdentifier) {
    UUID contextId = randomUUID();
    OrganisationEntity organisation = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(contextId)
        .name(callSiteIdentifier + "-organisation")
        .slug(contextId.toString())
        .externalId(contextId.toString())
        .build()
    );

    User user = new UserBuilder()
      .name("integration-test-user")
      .email(contextId.toString() + "@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .asUser()
      .build();
    users.create(user);

    User admin = new UserBuilder()
      .name("integration-test-admin")
      .email(contextId.toString() + "-admin@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .asAdmin()
      .build();
    users.create(admin);

    User superAdmin = new UserBuilder()
      .name("integration-test-super-admin")
      .email(contextId.toString() + "-super-admin@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation))
      .asSuperAdmin()
      .build();
    users.create(superAdmin);

    UUID contextId2 = randomUUID();
    OrganisationEntity organisation2 = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(contextId2)
        .name(callSiteIdentifier + "-organisation")
        .slug(contextId2.toString())
        .externalId(contextId2.toString())
        .build()
    );

    User user2 = new UserBuilder()
      .name("integration-test-user2")
      .email(contextId2.toString() + "@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
      .asUser()
      .build();
    users.create(user2);

    User admin2 = new UserBuilder()
      .name("integration-test-admin2")
      .email(contextId2.toString() + "-admin2@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
      .asAdmin()
      .build();
    users.create(admin2);

    User superAdmin2 = new UserBuilder()
      .name("integration-test-super-admin2")
      .email(contextId.toString() + "-super-admin2@test.com")
      .password("password")
      .organisation(OrganisationEntityMapper.toDomainModel(organisation2))
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
      superAdmin2,
      logicalMeters,
      gateways,
      physicalMeters,
      meterStatusLogs,
      meterAlarmLogs,
      measurements
    );
  }

  @Transactional
  public void destroy(IntegrationTestFixtureContext context) {
    Stream.of(context.organisationEntity.id, context.organisationEntity2.id)
      .forEach(id -> {
        try {
          organisationJpaRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignore) {
          // The test case probably removed it already
        }
      });
  }
}
