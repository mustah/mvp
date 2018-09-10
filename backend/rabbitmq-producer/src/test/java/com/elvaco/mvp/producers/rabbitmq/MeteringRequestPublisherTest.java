package com.elvaco.mvp.producers.rabbitmq;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringRequestPublisherTest {

  private SpyMessagePublisher spy;

  @Before
  public void setUp() {
    spy = new SpyMessagePublisher();
  }

  @Test
  public void regularUserCanNotIssueRequest() {
    MockAuthenticatedUser user = user();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);

    assertThatThrownBy(() -> meteringRequestPublisher.request(null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void adminCanNotIssueRequest() {
    MockAuthenticatedUser user = admin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);

    assertThatThrownBy(() -> meteringRequestPublisher.request(null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void superAdminCanIssueRequest() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.getPublishedMessages()).hasSize(1);
  }

  @Test
  public void meterOrganisationIsUsedInRequest() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).organisationId).isEqualTo(user.getOrganisation().externalId);
  }

  @Test
  public void meterOrganisationIsUsedInRequest_DifferentFromUserNativeOrganisation() {
    MockAuthenticatedUser user = superAdmin();
    Organisation otherOrganisation = new Organisation(
      UUID.randomUUID(),
      "other-organisation",
      "other-organisation",
      "other-organisation"
    );
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(
      user,
      new MockOrganisations(asList(user.getOrganisation(), otherOrganisation)),
      spy
    );
    LogicalMeter logicalMeter = newLogicalMeter(
      otherOrganisation.id,
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).organisationId).isEqualTo(otherOrganisation.externalId);
  }

  @Test
  public void meterExternalIdIsUsedAsFacilityIdInRequest() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).facility.id).isEqualTo(logicalMeter.externalId);
  }

  @Test
  public void physicalMeterAddressIsUsedAsMeterIdInRequest() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);
    PhysicalMeter physicalMeter = PhysicalMeter.builder().address("physical-meter-address").build();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      singletonList(physicalMeter),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).meter.id).isEqualTo("physical-meter-address");
  }

  @Test
  public void gatewayIdIsNotSet() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);
    PhysicalMeter physicalMeter = PhysicalMeter.builder().address("physical-meter-address").build();
    Gateway gateway = new Gateway(
      randomUUID(),
      user.getOrganisationId(),
      "gateway-serial",
      "gateway-product-model"
    );

    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      singletonList(physicalMeter),
      singletonList(gateway)
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).gateway).isEqualTo(null);
  }

  @Test
  public void exceptionFromMessagePublisherTriggersUpstreamServiceUnavailable() {
    MockAuthenticatedUser user = superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(
      user,
      new MockOrganisations(singletonList(user.getOrganisation())),
      messageBody -> {
        throw new RuntimeException("Something went horribly wrong!");
      }
    );
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    assertThatThrownBy(() -> meteringRequestPublisher.request(logicalMeter)).isInstanceOf(
      UpstreamServiceUnavailable.class);
  }

  private MeteringRequestPublisher makeMeteringRequestPublisher(MockAuthenticatedUser user) {
    return new MeteringRequestPublisher(
      user,
      new MockOrganisations(singletonList(user.getOrganisation())),
      spy
    );
  }

  private LogicalMeter newLogicalMeter(
    UUID organisationId,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways
  ) {
    UUID logicalMeterId = randomUUID();
    return LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId(logicalMeterId.toString())
      .organisationId(organisationId)
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .created(ZonedDateTime.now())
      .physicalMeters(physicalMeters)
      .gateways(gateways)
      .location(Location.UNKNOWN_LOCATION)
      .build();
  }

  private static MockAuthenticatedUser admin() {
    return new MockAuthenticatedUser(singletonList(Role.ADMIN));
  }

  private static MockAuthenticatedUser superAdmin() {
    return new MockAuthenticatedUser(singletonList(Role.SUPER_ADMIN));
  }

  private static MockAuthenticatedUser user() {
    return new MockAuthenticatedUser(singletonList(Role.USER));
  }
}
