package com.elvaco.mvp.producers.rabbitmq.dto;

import com.google.gson.annotations.SerializedName;

public enum MessageType {
  @SerializedName("Elvaco MVP MQ Measurement Message 1.0")
  METERING_MEASUREMENT_V_1_0,

  @SerializedName("Elvaco MVP MQ Alarm Message 1.0")
  METERING_ALARM_V_1_0,

  @SerializedName("Elvaco MVP MQ Reference Info Message 1.0")
  METERING_REFERENCE_INFO_V_1_0,

  @SerializedName("Elvaco MVP MQ Get Reference Info Message 1.0")
  METERING_GET_REFERENCE_INFO_V_1_0,

}
