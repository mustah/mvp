package com.elvaco.mvp.producers.rabbitmq;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.exception.UpstreamServiceUnavailable;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeteringRequestPublisher {

  private final AuthenticatedUser authenticatedUser;
  private final Organisations organisations;
  private final MessagePublisher messagePublisher;

  public void request(LogicalMeter logicalMeter) {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized("User '" + authenticatedUser.getUsername() + "' is not allowed to "
                             + "publish synchronization requests");
    }

    Organisation meterOrganisation = organisations.findById(logicalMeter.organisationId)
      .orElseThrow(
        () -> new RuntimeException(String.format(
          "Owning organisation %s of logical meter %s not found!",
          logicalMeter.organisationId,
          logicalMeter.id
        ))
      );

    GetReferenceInfoDto getReferenceInfoDto = GetReferenceInfoDto.builder()
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
  }

}
