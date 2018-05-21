package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;

@FunctionalInterface
public interface StructureMessageConsumer {

  void accept(MeteringStructureMessageDto message);
}
