package com.elvaco.mvp.producers.rabbitmq;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

public class MeteringRequestPublisher {

  private final AuthenticatedUser authenticatedUser;
  private final MessagePublisher messagePublisher;

  public MeteringRequestPublisher(
    AuthenticatedUser authenticatedUser,
    MessagePublisher messagePublisher
  ) {
    this.authenticatedUser = authenticatedUser;
    this.messagePublisher = messagePublisher;
  }

  void request(LogicalMeter logicalMeter) {
    if (!authenticatedUser.isSuperAdmin()) {
      throw new Unauthorized("User '" + authenticatedUser.getUsername() + "' is not allowed to "
        + "publish synchronization requests");
    }

    GetReferenceInfoDto getReferenceInfoDto = GetReferenceInfoDto.builder()
      .organisationExternalId(authenticatedUser.getOrganisationExternalId())
      .facilityId(logicalMeter.externalId)
      .meterExternalId(logicalMeter.activePhysicalMeter()
        .map(physicalMeter -> physicalMeter.address)
        .orElse(null))
      .build();

    messagePublisher.publish(MessageSerializer.toJson(getReferenceInfoDto).getBytes());
  }

}
