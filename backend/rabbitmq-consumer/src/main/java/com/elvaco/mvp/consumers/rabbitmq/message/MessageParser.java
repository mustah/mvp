package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

@FunctionalInterface
public interface MessageParser {

  MeteringMessageDto parse(String message);
}
