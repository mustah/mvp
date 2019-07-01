package com.elvaco.mvp.producers.rabbitmq;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.RequiredArgsConstructor;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
public class MeteringRequestPublisher {

  private final AuthenticatedUser authenticatedUser;
  private final Organisations organisations;
  private final MessagePublisher messagePublisher;
  private final JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;

  public String request(LogicalMeter logicalMeter) {
    ensureSuperUser();

    Organisation organisation = findOrganisationOrElseThrow(
      logicalMeter.organisationId,
      "logical meter",
      logicalMeter.id
    );

    GetReferenceInfoDto getReferenceInfoDto = GetReferenceInfoDto.builder()
      .jobId(randomUUID().toString())
      .organisationId(organisation.externalId)
      .facility(new FacilityIdDto(logicalMeter.externalId))
      .meter(logicalMeter.activePhysicalMeter()
        .map(physicalMeter -> new MeterIdDto(physicalMeter.address))
        .orElse(null))
      .build();

    publishMessage(getReferenceInfoDto);

    return getReferenceInfoDto.jobId;
  }

  public String request(Gateway gateway) {
    ensureSuperUser();

    findOrganisationOrElseThrow(gateway.organisationId, "gateway", gateway.id);

    Organisation organisation = findOrganisationOrElseThrow(
      gateway.organisationId,
      "gateway",
      gateway.id
    );

    GetReferenceInfoDto getReferenceInfoDto = GetReferenceInfoDto.builder()
      .jobId(randomUUID().toString())
      .organisationId(organisation.externalId)
      .gateway(new GatewayIdDto(gateway.serial))
      .build();

    publishMessage(getReferenceInfoDto);

    return getReferenceInfoDto.jobId;
  }

  private void publishMessage(GetReferenceInfoDto getReferenceInfoDto) {
    try {
      messagePublisher.publish(
        MessageSerializer.toJson(getReferenceInfoDto).getBytes(StandardCharsets.UTF_8)
      );
    } catch (Exception exception) {
      throw new UpstreamServiceUnavailable(exception.getMessage());
    }
    meterSyncJobService.newPendingJob(getReferenceInfoDto.jobId);
  }

  private Organisation findOrganisationOrElseThrow(
    UUID organisationId,
    String entityName,
    UUID id
  ) {
    return organisations.findById(organisationId)
      .orElseThrow(() -> new RuntimeException(String.format(
        "Owning organisation %s of %s %s not found!",
        organisationId,
        entityName,
        id
      )));
  }

  private void ensureSuperUser() {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized(String.format(
        "User '%s' is not allowed to publish synchronization requests",
        authenticatedUser.getUsername()
      ));
    }
  }
}
