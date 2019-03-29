package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.OTHER_ORGANISATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LogicalMeterUseCasesTest extends DefaultTestFixture {

  @Test
  public void shouldFindOrganisationsMeterById() {
    LogicalMeter meter = newMeter(randomUUID(), OTHER_ORGANISATION.id);
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.USER)),
      List.of(meter)
    );

    assertThat(useCases.findById(meter.id)).isNotEmpty();
  }

  @Test
  public void shouldNotFindOtherOrganisationsMeterById() {
    LogicalMeter meter = newMeter(randomUUID(), randomUUID());
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.USER)),
      List.of(meter)
    );

    assertThat(useCases.findById(meter.id)).isEmpty();
  }

  @Test
  public void superAdminShouldFindAllMeters() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.SUPER_ADMIN)),
      asList(
        newMeter(randomUUID(), OTHER_ORGANISATION.id),
        newMeter(randomUUID(), randomUUID()),
        newMeter(randomUUID(), OTHER_ORGANISATION.id)
      )
    );

    assertThat(useCases.findAllWithDetails(new MockRequestParameters())).hasSize(3);
  }

  @Test
  public void shouldOnlyFindAllMetersBelongingToOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.USER)),
      asList(
        newMeter(randomUUID(), OTHER_ORGANISATION.id),
        newMeter(randomUUID(), randomUUID()),
        newMeter(randomUUID(), OTHER_ORGANISATION.id)
      )
    );

    assertThat(useCases.findAllWithDetails(new MockRequestParameters())).hasSize(2);
  }

  @Test
  public void shouldFindMetersAsSubOrganisationUser() {
    var subOrganisation = subOrganisation().build();
    var user = newUser().organisation(subOrganisation).build();
    var meter = newMeter(randomUUID(), subOrganisation.parent.id);
    LogicalMeterUseCases useCases = newUseCases(
      new MockAuthenticatedUser(user, randomUUID().toString()),
      asList(meter)
    );

    assertThat(useCases.findById(meter.id)).isPresent();
  }

  @Test
  public void notAllowedToCreateMeterForOtherOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.USER)),
      emptyList()
    );

    assertThatThrownBy(() -> useCases.save(newMeter(randomUUID(), randomUUID())))
      .hasMessageContaining("not allowed");
  }

  @Test
  public void allowedToCreateMeterForOwnOrganisation() {
    LogicalMeterUseCases useCases = newUseCases(
      newAuthenticatedUser(List.of(Role.USER)),
      emptyList()
    );
    UUID meterId = randomUUID();

    LogicalMeter saved = useCases.save(newMeter(meterId, OTHER_ORGANISATION.id));

    assertThat(saved.id).isEqualTo(meterId);
  }

  private LogicalMeter newMeter(UUID meterId, UUID organisationId) {
    return LogicalMeter.builder()
      .id(meterId)
      .externalId("meter-" + meterId)
      .organisationId(organisationId)
      .build();
  }

  private AuthenticatedUser newAuthenticatedUser(List<Role> roles) {
    return new MockAuthenticatedUser(
      new UserBuilder()
        .name("mocked user")
        .email("mock@mock.net")
        .password("password")
        .organisation(OTHER_ORGANISATION)
        .roles(roles)
        .build(),
      "some-token"
    );
  }

  private LogicalMeterUseCases newUseCases(
    AuthenticatedUser authenticatedUser,
    List<LogicalMeter> logicalMeters
  ) {
    return new LogicalMeterUseCases(authenticatedUser, new MockLogicalMeters(logicalMeters));
  }
}
