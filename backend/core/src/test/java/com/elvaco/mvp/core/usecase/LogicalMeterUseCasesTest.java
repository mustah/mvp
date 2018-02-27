package com.elvaco.mvp.core.usecase;

import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterUseCasesTest {

  @Test
  public void shouldFindOrganisationsMeterById() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(1L, singletonList(Role.USER)),
      singletonList(newMeter(1L, 1L))
    );

    assertThat(
      useCases.findById(1L)
    ).isNotEmpty();
  }

  @Test
  public void shouldNotFindOtherOrganisationsMeterById() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(1L, singletonList(Role.USER)),
      singletonList(newMeter(1L, 2L))
    );

    assertThat(
      useCases.findById(1L)
    ).isEmpty();
  }

  @Test
  public void superAdminShouldFindAllMeters() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(1L, singletonList(Role.SUPER_ADMIN)),
      asList(
        newMeter(0L, 1L),
        newMeter(1L, 2L),
        newMeter(2L, 1L)
      )
    );

    assertThat(useCases.findAll()).hasSize(3);
  }

  @Test
  public void shouldOnlyFindAllMetersBelongingToOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(1L, singletonList(Role.USER)),
      asList(
        newMeter(0L, 1L),
        newMeter(1L, 2L),
        newMeter(2L, 1L)
      )
    );

    assertThat(useCases.findAll()).hasSize(2);
  }

  private LogicalMeter newMeter(long meterId, long organisationId) {
    return new LogicalMeter(
      meterId,
      "meter-" + meterId,
      organisationId,
      Location.UNKNOWN_LOCATION,
      new Date()
    );
  }

  private AuthenticatedUser newAuthenticatedUser(Long organisationId, List<Role> roles) {
    return new MockAuthenticatedUser(
      new User(
        0L,
        "mocked user",
        "mock@mock.net",
        "password",
        new Organisation(organisationId, "some organisation", "some-org"),
        roles
      ),
      "some-token"
    );
  }

  private LogicalMeterUseCases newUseCases(
    AuthenticatedUser authenticatedUser,
    List<LogicalMeter> logicalMeters
  ) {
    return new LogicalMeterUseCases(
      authenticatedUser,
      new MockLogicalMeters(logicalMeters),
      null
    );
  }
}

