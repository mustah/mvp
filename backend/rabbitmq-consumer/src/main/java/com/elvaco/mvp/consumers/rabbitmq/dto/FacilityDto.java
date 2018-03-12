package com.elvaco.mvp.consumers.rabbitmq.dto;

public class FacilityDto {
  public final String id;
  public final String country;
  public final String city;
  public final String address;

  public FacilityDto(String id, String country, String city, String address) {
    this.id = id;
    this.country = country;
    this.city = city;
    this.address = address;
  }
}
