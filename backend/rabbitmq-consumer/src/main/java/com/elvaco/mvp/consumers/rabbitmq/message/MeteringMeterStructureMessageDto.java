package com.elvaco.mvp.consumers.rabbitmq.message;


public class MeteringMeterStructureMessageDto extends MeteringMessageDto {

  public final String meterId;
  public final String facilityId;
  public final String medium;
  public final int expectedInterval;
  public final String sourceSystemId;
  public final String organisationId;
  public final String manufacturer;
  public final GatewayDto gateway;
  public final LocationDto location;

  public MeteringMeterStructureMessageDto(
    MessageType messageType,
    String meterId,
    String facilityId,
    String medium,
    int expectedInterval,
    String sourceSystemId,
    String organisationId,
    String manufacturer,
    GatewayDto gateway,
    LocationDto location
  ) {
    super(messageType);
    this.meterId = meterId;
    this.facilityId = facilityId;
    this.medium = medium;
    this.expectedInterval = expectedInterval;
    this.sourceSystemId = sourceSystemId;
    this.organisationId = organisationId;
    this.manufacturer = manufacturer;
    this.gateway = gateway;
    this.location = location;
  }
}
