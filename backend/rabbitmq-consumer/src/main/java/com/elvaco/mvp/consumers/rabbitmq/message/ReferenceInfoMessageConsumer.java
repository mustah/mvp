package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

@FunctionalInterface
public interface ReferenceInfoMessageConsumer {

  void accept(MeteringReferenceInfoMessageDto message);
}
