package com.elvaco.mvp.producers.rabbitmq;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

class SpyMessagePublisher implements MessagePublisher {

  private final List<byte[]> publishedMessages;

  SpyMessagePublisher() {
    this.publishedMessages = new ArrayList<>();
  }

  @Override
  public void publish(byte[] messageBody) {
    publishedMessages.add(messageBody);
  }

  List<byte[]> getPublishedMessages() {
    return publishedMessages;
  }

  GetReferenceInfoDto deserialize(int index) {
    return fromBytes(publishedMessages.get(index));
  }

  private GetReferenceInfoDto fromBytes(byte[] messageBody) {
    return MessageSerializer.fromJson(
      new String(messageBody, StandardCharsets.UTF_8),
      GetReferenceInfoDto.class
    );
  }
}
