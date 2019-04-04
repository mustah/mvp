package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Optional;

import com.elvaco.mvp.producers.rabbitmq.dto.MeteringMessageDto;

@FunctionalInterface
public interface MessageParser {

  Optional<MeteringMessageDto> parse(String message);
}
