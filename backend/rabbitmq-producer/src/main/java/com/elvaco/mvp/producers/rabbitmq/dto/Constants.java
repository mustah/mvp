package com.elvaco.mvp.producers.rabbitmq.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

  public static final MeteringReferenceInfoMessageDto NULL_METERING_REFERENCE_INFO_MESSAGE_DTO =
    new MeteringReferenceInfoMessageDto(null, null, null, null, null, null);

}
