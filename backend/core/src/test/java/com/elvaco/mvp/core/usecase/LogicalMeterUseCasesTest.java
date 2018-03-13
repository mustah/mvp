package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
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
import org.junit.Ignore;
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
    ZonedDateTime after = parseDateTime("2001-01-01T13:00:00Z");
    ZonedDateTime before = parseDateTime("2001-01-01T14:00:00Z");

    assertThat(LogicalMeterUseCases.calculatedExpectedReadOuts(15, after, before))
      .as("Unexpected nr of read outs")
      .isEqualTo(4);
  }

  @Test
  public void nrOfReadOutsInDay() {
    ZonedDateTime after = parseDateTime("2001-01-01T00:00:00Z");
    ZonedDateTime before = parseDateTime("2001-01-02T00:00:00Z");

    assertThat(LogicalMeterUseCases.calculatedExpectedReadOuts(60, after, before))
      .as("Unexpected nr of read outs")
      .isEqualTo(24);
  }

  @Test
  public void getFirstQuarterInterval() {
    assertThat(getFirstMatching("2001-01-01T10:31:00.00Z", 15))
      .as("Failed to advance 10:31 to first 15 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:45:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:00:00.00Z", 15))
      .as("Failed to advance 10:00 to first 15 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:16:00.00Z", 15))
      .as("Failed to advance 10:16 to next 15 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:30:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:14:00.00Z", 15))
      .as("Failed to advance 10:14 to next 15 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:15:00.00Z"));
  }

  @Test
  public void getFirst30MinInterval() {
    assertThat(getFirstMatching("2001-01-01T10:31:00.00Z", 30))
      .as("Failed to advance 10:31 to first 30 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T11:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:00:00.00Z", 30))
      .as("Failed to advance 10:00 to first 30 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:50:00.00Z", 30))
      .as("Failed to advance 10:50 to first 30 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T11:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:16:00.00Z", 30))
      .as("Failed to advance 10:16 to next 30 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:30:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:14:00.00Z", 30))
      .as("Failed to advance 10:14 to next 30 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:30:00.00Z"));
  }

  @Test
  public void getFirst20MinInterval() {
    assertThat(getFirstMatching("2001-01-01T10:31:00.00Z", 20))
      .as("Failed to advance 10:31 to first 20 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:40:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:00:00.00Z", 20))
      .as("Failed to advance 10:00 to first 20 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:16:00.00Z", 20))
      .as("Failed to advance 10:16 to next 20 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:20:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:14:00.00Z", 20))
      .as("Failed to advance 10:14 to next 20 min interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:20:00.00Z"));
  }

  @Test
  public void getFirstHourInterval() {
    assertThat(getFirstMatching("2001-01-01T10:31:00.00Z", 60))
      .as("Failed to advance 10:31 to first hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T11:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:00:00.00Z", 60))
      .as("Failed to advance 10:00 to first hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T10:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:16:00.00Z", 60))
      .as("Failed to advance 10:16 to next hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T11:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:14:00.00Z", 60))
      .as("Failed to advance 10:14 to next hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-01T11:00:00.00Z"));
  }

  @Ignore
  @Test
  public void getFirst24HourInterval() {
    assertThat(getFirstMatching("2001-01-01T10:31:00.00Z", 1440))
      .as("Failed to advance 10:31 to first 24 hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-02T00:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:00:00.00Z", 1440))
      .as("Failed to advance 10:00 to first 24 hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-02T00:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:16:00.00Z", 1440))
      .as("Failed to advance 10:16 to first 24 hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-02T00:00:00.00Z"));

    assertThat(getFirstMatching("2001-01-01T10:14:00.00Z", 1440))
      .as("Failed to advance 10:14 to first 24 hour interval")
      .isEqualTo(ZonedDateTime.parse("2001-01-02T00:00:00.00Z"));
  }

  private ZonedDateTime getFirstMatching(String date, long interval) {
    return LogicalMeterUseCases.getFirstDateMatchingInterval(ZonedDateTime.parse(date), interval);
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

