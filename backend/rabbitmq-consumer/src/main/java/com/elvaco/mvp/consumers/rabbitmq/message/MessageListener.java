package com.elvaco.mvp.consumers.rabbitmq.message;

import javax.annotation.Nullable;

@FunctionalInterface
public interface MessageListener {

  /**
   * Listener for messages on RabbitMQ queue. These messages are later parsed by consumers.
   *
   * @param message Encoded message received from queue.
   *
   * @return A serialized json string containing response information to be placed on the response
   *   queue. When this is {@code null} no message will be places on the response routing queue.
   */
  @Nullable
  String onMessage(String message);
}
