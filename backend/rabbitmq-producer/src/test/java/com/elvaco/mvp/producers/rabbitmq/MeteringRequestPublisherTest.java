package com.elvaco.mvp.producers.rabbitmq;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringRequestPublisherTest {

  @Test
  public void regularUserCanNotIssueRequest() {
    AuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role.USER));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    assertThatThrownBy(() -> meteringRequestPublisher.request(null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void adminCanNotIssueRequest() {
    AuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role.ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    assertThatThrownBy(() -> meteringRequestPublisher.request(null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void superAdminCanIssueRequest() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    List<byte[]> publishedMessages = spy.getPublishedMessages();
    assertThat(publishedMessages).hasSize(1);
  }

  @Test
  public void meterOrganisationIsUsedInRequest() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).organisationExternalId).isEqualTo(user
      .getOrganisationExternalId());
  }

  @Test
  public void meterExternalIdIsUsedAsFacilityIdInRequest() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).facilityId).isEqualTo(logicalMeter.externalId);
  }

  @Test
  public void physicalMeterAddressIsUsedAsMeterIdInRequest() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
    PhysicalMeter physicalMeter = PhysicalMeter.builder().address("physical-meter-address").build();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      singletonList(physicalMeter),
      emptyList()
    );

    meteringRequestPublisher.request(logicalMeter);

    assertThat(spy.deserialize(0).meterExternalId).isEqualTo("physical-meter-address");
  }

  @Test
  public void gatewayIdIsNotSet() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    SpyMessagePublisher spy = new SpyMessagePublisher();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(user, spy);
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

    assertThat(spy.deserialize(0).gatewayExternalId).isEqualTo(null);
  }

  @Test
  public void exceptionFromMessagePublisherTriggersUpstreamServiceUnavailable() {
    MockAuthenticatedUser user = new MockAuthenticatedUser(Collections.singletonList(Role
      .SUPER_ADMIN));
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(
      user,
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

  private LogicalMeter newLogicalMeter(
    UUID organisationId,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways
  ) {
    UUID logicalMeterId = randomUUID();
    return new LogicalMeter(
      logicalMeterId,
      logicalMeterId.toString(),
      organisationId,
      Location.UNKNOWN_LOCATION,
      ZonedDateTime.now(),
      physicalMeters,
      MeterDefinition.UNKNOWN_METER,
      gateways
    );
  }
}
