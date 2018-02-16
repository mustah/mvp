package com.elvaco.mvp.consumers.rabbitmq.message;

public class LocationDto {
  public final String country;
  public final String city;
  public final String address;

  public LocationDto(String country, String city, String address) {
    this.country = country;
    this.city = city;
    this.address = address;
  }
}
