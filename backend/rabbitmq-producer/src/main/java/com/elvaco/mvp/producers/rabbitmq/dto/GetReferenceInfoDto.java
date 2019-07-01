package com.elvaco.mvp.producers.rabbitmq.dto;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class GetReferenceInfoDto extends MeteringMessageDto {

  @SerializedName("source_system_id")
  private static String SOURCE_SYSTEM_ID = "Elvaco Evo";

  public final String organisationId;

  @Nullable
  public final String jobId;

  @Nullable
  public final IdDto meter;

  @Nullable
  public final IdDto gateway;

  @Nullable
  public final IdDto facility;

  public GetReferenceInfoDto(
    String organisation,
    @Nullable String jobId,
    @Nullable IdDto meter,
    @Nullable IdDto gateway,
    @Nullable IdDto facility
  ) {
    super(MessageType.METERING_GET_REFERENCE_INFO_V_1_0);
    this.organisationId = organisation;
    this.jobId = jobId;
    this.meter = meter;
    this.gateway = gateway;
    this.facility = facility;
  }
}
