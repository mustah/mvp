package com.elvaco.mvp.producers.rabbitmq;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeteringRequestPublisher {

  private final AuthenticatedUser authenticatedUser;
  private final Organisations organisations;
  private final MessagePublisher messagePublisher;
  private final JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;

  public String request(LogicalMeter logicalMeter) {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized(String.format(
        "User '%s' is not allowed to publish synchronization requests",
        authenticatedUser.getUsername()
      ));
    }

    Organisation meterOrganisation = organisations.findById(logicalMeter.organisationId)
      .orElseThrow(
        () -> new RuntimeException(String.format(
          "Owning organisation %s of logical meter %s not found!",
          logicalMeter.organisationId,
          logicalMeter.id
        ))
      );
    String jobId = UUID.randomUUID().toString();

    GetReferenceInfoDto getReferenceInfoDto = GetReferenceInfoDto.builder()
      .jobId(jobId)
      .organisationId(meterOrganisation.externalId)
      .facility(new FacilityIdDto(logicalMeter.externalId))
      .meter(logicalMeter.activePhysicalMeter()
        .map(physicalMeter -> new MeterIdDto(physicalMeter.address))
        .orElse(null))
      .build();

    try {
      messagePublisher.publish(MessageSerializer.toJson(getReferenceInfoDto).getBytes());
    } catch (Exception exception) {
      throw new UpstreamServiceUnavailable(exception.getMessage());
    }
    meterSyncJobService.newPendingJob(jobId);
    return getReferenceInfoDto.jobId;
  }
}
