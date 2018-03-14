package com.elvaco.mvp.core.usecase;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.util.Dates.parseDateTime;
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
    LogicalMeter meter = newMeter(randomUUID(), organisation.id);
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      singletonList(meter)
    );

    assertThat(useCases.findById(meter.id)).isNotEmpty();
  }

  @Test
  public void shouldNotFindOtherOrganisationsMeterById() {
    LogicalMeter meter = newMeter(randomUUID(), randomUUID());
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      singletonList(meter)
    );

    assertThat(useCases.findById(meter.id)).isEmpty();
  }

  @Test
  public void superAdminShouldFindAllMeters() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.SUPER_ADMIN)),
      asList(
        newMeter(randomUUID(), organisation.id),
        newMeter(randomUUID(), randomUUID()),
        newMeter(randomUUID(), organisation.id)
      )
    );

    assertThat(useCases.findAll()).hasSize(3);
  }

  @Test
  public void shouldOnlyFindAllMetersBelongingToOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      asList(
        newMeter(randomUUID(), organisation.id),
        newMeter(randomUUID(), randomUUID()),
        newMeter(randomUUID(), organisation.id)
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

    assertThatThrownBy(() -> useCases.save(newMeter(randomUUID(), randomUUID())))
      .hasMessageContaining("not allowed");
  }

  @Test
  public void allowedToCreateMeterForOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(singletonList(Role.USER)),
      emptyList()
    );
    UUID meterId = randomUUID();

    LogicalMeter saved = useCases.save(newMeter(meterId, organisation.id));

    assertThat(saved.id).isEqualTo(meterId);
  }

  @Test
  public void nrOfReadOutsInHour() {
    LocalDateTime after = parseDateTime("2001-01-01T13:00:00Z");
    LocalDateTime before = parseDateTime("2001-01-01T14:00:00Z");

    assertThat(LogicalMeterUseCases.calculatedExpectedReadOuts(15, after, before))
      .as("Unexpected nr of read outs")
      .isEqualTo(4);
  }

  @Test
  public void nrOfReadOutsInDay() {
    LocalDateTime after = parseDateTime("2001-01-01T00:00:00Z");
    LocalDateTime before = parseDateTime("2001-01-02T00:00:00Z");

    assertThat(LogicalMeterUseCases.calculatedExpectedReadOuts(60, after, before))
      .as("Unexpected nr of read outs")
      .isEqualTo(24);
  }

  private LogicalMeter newMeter(UUID meterId, UUID organisationId) {
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
      new UserBuilder()
        .name("mocked user")
        .email("mock@mock.net")
        .password("password")
        .organisation(organisation)
        .roles(roles)
        .build(),
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

