package com.elvaco.mvp.producers.rabbitmq;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testing.amqp.MockJobService;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringRequestPublisherTest {

  private SpyMessagePublisher spy;
  private MockJobService<MeteringReferenceInfoMessageDto> jobService;

  @Before
  public void setUp() {
    spy = new SpyMessagePublisher();
    jobService = new MockJobService<>();
  }

  @Test
  public void regularUserCanNotIssueRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.mvpUser();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);

    assertThatThrownBy(() -> meteringRequestPublisher.request((LogicalMeter) null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void adminCanNotIssueRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.mvpAdmin();
    MeteringRequestPublisher meteringRequestPublisher = makeMeteringRequestPublisher(user);

    assertThatThrownBy(() -> meteringRequestPublisher.request((LogicalMeter) null))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("not allowed to publish synchronization requests");

    assertThat(spy.getPublishedMessages()).isEmpty();
  }

  @Test
  public void superAdminCanIssueRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    makeMeteringRequestPublisher(user).request(logicalMeter);

    assertThat(spy.getPublishedMessages()).hasSize(1);
  }

  @Test
  public void meterOrganisationIsUsedInRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    makeMeteringRequestPublisher(user).request(logicalMeter);

    assertThat(spy.deserialize(0).organisationId).isEqualTo(user.getOrganisation().externalId);
  }

  @Test
  public void meterOrganisationIsUsedInRequest_DifferentFromUserNativeOrganisation() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    Organisation otherOrganisation = Organisation.of("other organisation");
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(
      user,
      new MockOrganisations(List.of(user.getOrganisation(), otherOrganisation)),
      spy,
      jobService
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
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    makeMeteringRequestPublisher(user).request(logicalMeter);

    assertThat(spy.deserialize(0).facility.id).isEqualTo(logicalMeter.externalId);
  }

  @Test
  public void physicalMeterAddressIsUsedAsMeterIdInRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .activePeriod(PeriodRange.unbounded())
      .address("physical-meter-address")
      .build();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      List.of(physicalMeter),
      emptyList()
    );

    makeMeteringRequestPublisher(user).request(logicalMeter);

    assertThat(spy.deserialize(0).meter.id).isEqualTo("physical-meter-address");
  }

  @Test
  public void jobIdIsReturnedAndSetInRequest() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      emptyList(),
      emptyList()
    );

    String jobId = makeMeteringRequestPublisher(user).request(logicalMeter);

    String actual = spy.deserialize(0).jobId;
    assertThat(jobId).isNotEmpty();
    assertThat(actual).isEqualTo(jobId).isEqualTo(jobId);
  }

  @Test
  public void gatewayIdIsNotSet() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .address("physical-meter-address")
      .build();

    Gateway gateway = Gateway.builder()
      .organisationId(user.getOrganisationId())
      .serial("gateway-serial")
      .productModel("gateway-product-model")
      .build();

    LogicalMeter logicalMeter = newLogicalMeter(
      user.getOrganisationId(),
      List.of(physicalMeter),
      List.of(gateway)
    );

    makeMeteringRequestPublisher(user).request(logicalMeter);

    assertThat(spy.deserialize(0).gateway).isEqualTo(null);

    assertThat(jobService.getAll().size()).isEqualTo(1);
  }

  @Test
  public void syncGatewayId() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    Gateway gateway = Gateway.builder()
      .organisationId(user.getOrganisationId())
      .serial("gateway-serial")
      .productModel("gateway-product-model")
      .build();

    makeMeteringRequestPublisher(user).request(gateway);

    assertThat(spy.deserialize(0).gateway.id).isEqualTo("gateway-serial");

    assertThat(jobService.getAll().size()).isEqualTo(1);
  }

  @Test
  public void exceptionFromMessagePublisherTriggersUpstreamServiceUnavailable() {
    MockAuthenticatedUser user = MockAuthenticatedUser.superAdmin();
    MeteringRequestPublisher meteringRequestPublisher = new MeteringRequestPublisher(
      user,
      new MockOrganisations(List.of(user.getOrganisation())),
      messageBody -> {
        throw new RuntimeException("Something went horribly wrong!");
      },
      jobService
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
      new MockOrganisations(List.of(user.getOrganisation())),
      spy,
      jobService
    );
  }

  private LogicalMeter newLogicalMeter(
    UUID organisationId,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways
  ) {
    return LogicalMeter.builder()
      .externalId(randomUUID().toString())
      .organisationId(organisationId)
      .physicalMeters(physicalMeters)
      .gateways(gateways)
      .build();
  }
}
