package com.elvaco.mvp.core.usecase;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogicalMeterUseCasesTest {

  private Organisation organisation;

  @Before
  public void setUp() {
    organisation = new Organisation(randomUUID(), "some organisation", "some-org");
  }

  @Test
  public void shouldFindOrganisationsMeterById() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      singletonList(newMeter(1L, organisation.id))
    );

    assertThat(useCases.findById(1L)).isNotEmpty();
  }

  @Test
  public void shouldNotFindOtherOrganisationsMeterById() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      singletonList(newMeter(1L, randomUUID()))
    );

    assertThat(useCases.findById(1L)).isEmpty();
  }

  @Test
  public void superAdminShouldFindAllMeters() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.SUPER_ADMIN)),
      asList(
        newMeter(0L, organisation.id),
        newMeter(1L, randomUUID()),
        newMeter(2L, organisation.id)
      )
    );

    assertThat(useCases.findAll()).hasSize(3);
  }

  @Test
  public void shouldOnlyFindAllMetersBelongingToOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      asList(
        newMeter(0L, organisation.id),
        newMeter(1L, randomUUID()),
        newMeter(2L, organisation.id)
      )
    );

    assertThat(useCases.findAll()).hasSize(2);
  }

  @Test
  public void notAllowedToCreateMeterForOtherOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      emptyList()
    );

    assertThatThrownBy(() -> useCases.save(newUnsavedMeter(randomUUID())))
      .hasMessageContaining("not allowed");
  }

  @Test
  public void allowedToCreateMeterForOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      emptyList()
    );

    assertThat(useCases.save(newUnsavedMeter(organisation.id)).id).isEqualTo(1L);
  }

  private LogicalMeter newUnsavedMeter(UUID organisationId) {
    return newMeter(null, organisationId);
  }

  private LogicalMeter newMeter(Long meterId, UUID organisationId) {
    return new LogicalMeter(
      meterId,
      "meter-" + meterId,
      organisationId,
      Location.UNKNOWN_LOCATION,
      new Date()
    );
  }

  private AuthenticatedUser newAuthenticatedUser(List<Role> roles) {
    return new MockAuthenticatedUser(
      new User(
        randomUUID(),
        "mocked user",
        "mock@mock.net",
        "password",
        organisation,
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

