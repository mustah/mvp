package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.core.usecase.GatewayUseCases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class InfrastructureStatusMessageConsumer
  implements InfrastructureMessageConsumer {

  private final GatewayUseCases gatewayUseCases;

  @Override
  public void accept(InfrastructureStatusMessageDto message) {
    var gateways = gatewayUseCases.findBy(message.eui);

    if (gateways.size() > 1) {
      log.warn("Ignoring Infrastructure Status Message, ambiguous gateways for "
        + message.toString() + ". Found gateways " + gateways);
    } else {
      gateways.stream()
        .findFirst()
        .ifPresentOrElse(
          gw -> gatewayUseCases.save(
            gw.toBuilder()
              .extraInfo(message.properties)
              .build()),
          () -> log.warn("Ignoring Infrastructure Status Message, gateway serial not found for "
            + message.toString())
        );
    }
  }
}
