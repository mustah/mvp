package com.elvaco.mvp.core.spi.amqp;

public interface MessagePublisher {
  void publish(byte[] messageBody);
}
