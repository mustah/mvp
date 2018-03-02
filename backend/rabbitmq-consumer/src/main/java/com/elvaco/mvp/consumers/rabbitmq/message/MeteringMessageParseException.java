package com.elvaco.mvp.consumers.rabbitmq.message;

public class MeteringMessageParseException extends RuntimeException {

  private static final long serialVersionUID = 7334788232403684436L;

  MeteringMessageParseException(String message) {
    super(message);
  }
}
