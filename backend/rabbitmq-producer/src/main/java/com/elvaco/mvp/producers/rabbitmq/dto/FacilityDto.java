package com.elvaco.mvp.producers.rabbitmq.dto;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class FacilityDto {
  public final String id;
  public final String country;
  public final String city;
  public final String address;
  public final String zip;
}
