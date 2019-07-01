package com.elvaco.mvp.producers.rabbitmq.dto;

import com.elvaco.mvp.core.domainmodels.Identifiable;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class IdDto implements Identifiable<String> {

  public final String id;

  @Override
  public String getId() {
    return id;
  }
}
