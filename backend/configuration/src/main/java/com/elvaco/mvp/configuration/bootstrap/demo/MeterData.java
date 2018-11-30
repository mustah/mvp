package com.elvaco.mvp.configuration.bootstrap.demo;

import javax.annotation.Nullable;

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
  @Nullable
  public final String ip;
  @Nullable
  public final String port;

  public final String gatewayId;
  public final String gatewayProductModel;
  public final String gatewayStatus;

  public final String utcOffset;

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
    String ip,
    String port,
    String meterStatus,
    String gatewayStatus,
    String utcOffset
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
    this.ip = toNull(ip);
    this.port = toNull(port);
    this.meterStatus = meterStatus;
    this.gatewayStatus = gatewayStatus;
    this.utcOffset = utcOffset;
  }

  @Nullable
  private static String toNull(String value) {
    return value.equals("NULL") ? null : value;
  }
}
