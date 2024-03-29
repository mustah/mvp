package com.elvaco.mvp.testdata;

import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.testing.fixture.UserBuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
class IntegrationTestFixtureContextFactory {

  private final Organisations organisations;
  private final Users users;
  private final UserSelections userSelections;
  private final LogicalMeters logicalMeters;
  private final PhysicalMeters physicalMeters;
  private final MeterStatusLogs meterStatusLogs;
  private final MeterAlarmLogs meterAlarmLogs;
  private final Measurements measurements;
  private final Gateways gateways;
  private final GatewayStatusLogs gatewayStatusLogs;
  private final MeterDefinitions meterDefinitions;
  private final Dashboards dashboards;
  private final Widgets widgets;

  @Transactional
  public IntegrationTestFixtureContext create(String callSiteIdentifier) {
    var contextId = randomUUID();
    var name = callSiteIdentifier + "-organisation";
    var organisation = organisations.saveAndFlush(Organisation.of(name, contextId));

    User user = new UserBuilder()
      .name("integration-test-user")
      .email(contextId.toString() + "@test.com")
      .password("password")
      .organisation(organisation)
      .asMvpUser()
      .build();
    users.save(user);

    User mvpAdmin = new UserBuilder()
      .name("integration-test-mvpAdmin")
      .email(contextId.toString() + "-mvpAdmin@test.com")
      .password("password")
      .organisation(organisation)
      .asMvpAdmin()
      .build();
    users.save(mvpAdmin);

    User otcAdmin = new UserBuilder()
      .name("integration-test-otcAdmin")
      .email(contextId.toString() + "-otcAdmin@test.com")
      .password("password")
      .organisation(organisation)
      .asOtcAdmin()
      .build();
    users.save(otcAdmin);

    User superAdmin = new UserBuilder()
      .name("integration-test-super-admin")
      .email(contextId.toString() + "-super-admin@test.com")
      .password("password")
      .organisation(organisation)
      .asSuperAdmin()
      .build();
    users.save(superAdmin);

    return new IntegrationTestFixtureContext(
      organisation,
      user,
      mvpAdmin,
      otcAdmin,
      superAdmin,
      logicalMeters,
      gateways,
      physicalMeters,
      meterStatusLogs,
      gatewayStatusLogs,
      meterAlarmLogs,
      measurements,
      meterDefinitions,
      organisations,
      users,
      userSelections,
      dashboards,
      widgets
    );
  }

  @Transactional
  public void destroy(IntegrationTestFixtureContext context) {
    Stream.of(context.organisation.id)
      .forEach(id -> {
        try {
          organisations.deleteById(id);
        } catch (EmptyResultDataAccessException ignore) {
          // The test case probably removed it already
        }
      });
  }
}
