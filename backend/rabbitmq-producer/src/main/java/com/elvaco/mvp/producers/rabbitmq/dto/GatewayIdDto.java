package com.elvaco.mvp.producers.rabbitmq.dto;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class GatewayIdDto {

  public final String id;
}
