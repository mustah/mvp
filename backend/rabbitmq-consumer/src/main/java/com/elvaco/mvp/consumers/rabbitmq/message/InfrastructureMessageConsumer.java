package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;

@FunctionalInterface
public interface InfrastructureMessageConsumer {

  void accept(InfrastructureStatusMessageDto message);
}
