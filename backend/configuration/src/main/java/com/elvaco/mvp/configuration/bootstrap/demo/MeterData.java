package com.elvaco.mvp.configuration.bootstrap.demo;

import lombok.ToString;

@ToString
public class MeterData {

  public final String meterId;
  public final String meterStatus;
  public final String facilityId;
  public final String address;
  public final String city;
  public final String medium;
  public final String meterManufacturer;
  public final String phone;
  public final String gatewayId;
  public final String gatewayProductModel;
  public final String gatewayStatus;

  public MeterData(
    String facilityId,
    String address,
    String city,
    String medium,
    String meterId,
    String meterManufacturer,
    String gatewayId,
    String gatewayProductModel,
    String phone,
    String meterStatus,
    String gatewayStatus
  ) {
    this.facilityId = facilityId;
    this.address = address;
    this.city = city;
    this.meterId = meterId;
    this.medium = medium;
    this.meterManufacturer = meterManufacturer;
    this.gatewayId = gatewayId;
    this.gatewayProductModel = gatewayProductModel;
    this.phone = phone;
    this.meterStatus = meterStatus;
    this.gatewayStatus = gatewayStatus;
  }

}
