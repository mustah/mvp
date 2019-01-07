package com.elvaco.mvp.producers.rabbitmq.dto;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MeteringReferenceInfoMessageDto extends MeteringMessageDto {

  @Nullable
  public final MeterDto meter;

  // Metering currently sends this in camel-case form, for some reason
  @SerializedName(value = "jobId", alternate = "job_id")
  @Nullable
  public final String jobId;

  @Nullable
  public final FacilityDto facility;

  public final String sourceSystemId;
  public final String organisationId;

  @Nullable
  public final GatewayStatusDto gateway;

  public MeteringReferenceInfoMessageDto(
    @Nullable MeterDto meter,
    @Nullable FacilityDto facility,
    String sourceSystemId,
    String organisationId,
    @Nullable GatewayStatusDto gateway,
    @Nullable String jobId
  ) {
    super(MessageType.METERING_REFERENCE_INFO_V_1_0);
    this.meter = meter;
    this.facility = facility;
    this.sourceSystemId = sourceSystemId;
    this.organisationId = organisationId;
    this.gateway = gateway;
    this.jobId = jobId;
  }

  public MeteringReferenceInfoMessageDto withFacility(FacilityDto facilityDto) {
    return new MeteringReferenceInfoMessageDto(
      meter,
      facilityDto,
      sourceSystemId,
      organisationId,
      gateway,
      jobId
    );
  }

  public MeteringReferenceInfoMessageDto withMeter(MeterDto meterDto) {
    return new MeteringReferenceInfoMessageDto(
      meterDto,
      facility,
      sourceSystemId,
      organisationId,
      gateway,
      jobId
    );
  }

  public MeteringReferenceInfoMessageDto withGatewayStatus(
    @Nullable GatewayStatusDto gatewayStatusDto
  ) {
    return new MeteringReferenceInfoMessageDto(
      meter,
      facility,
      sourceSystemId,
      organisationId,
      gatewayStatusDto,
      jobId
    );
  }
}
