package com.elvaco.mvp.producers.rabbitmq;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.spi.amqp.MessagePublisher;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;

class SpyMessagePublisher implements MessagePublisher {

  private final List<byte[]> publishedMessages;

  SpyMessagePublisher() {
    this.publishedMessages = new ArrayList<>();
  }

  List<byte[]> getPublishedMessages() {
    return publishedMessages;
  }

  @Override
  public void publish(byte[] messageBody) {
    publishedMessages.add(messageBody);
  }

  GetReferenceInfoDto deserialize(int index) {
    return fromBytes(publishedMessages.get(index));
  }

  private GetReferenceInfoDto fromBytes(byte[] messageBody) {
    try {
      return MessageSerializer.fromJson(
        new String(messageBody, "UTF-8"),
        GetReferenceInfoDto.class
      );
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
