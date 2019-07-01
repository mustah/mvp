package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

import com.elvaco.mvp.producers.rabbitmq.SyncRequestStatusType;
import com.elvaco.mvp.producers.rabbitmq.dto.Constants;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SyncRequestStatusDto {

  public String jobId;

  @Nullable
  public String response;

  public SyncRequestStatusType status;

  public static SyncRequestStatusDto from(
    String jobId,
    MeteringReferenceInfoMessageDto messageDto
  ) {
    SyncRequestStatusType status = statusFromMessageDto(messageDto);
    return new SyncRequestStatusDto(
      jobId,
      status == SyncRequestStatusType.COMPLETED ? messageDto.toString() : null,
      status
    );
  }

  private static SyncRequestStatusType statusFromMessageDto(
    MeteringReferenceInfoMessageDto messageDto
  ) {
    if (messageDto == null) {
      return SyncRequestStatusType.UNKNOWN;
    } else if (messageDto.equals(Constants.NULL_METERING_REFERENCE_INFO_MESSAGE_DTO)) {
      return SyncRequestStatusType.PENDING;
    } else {
      return SyncRequestStatusType.COMPLETED;
    }
  }
}
