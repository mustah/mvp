package com.elvaco.mvp.consumers.rabbitmq.message;

import com.google.gson.annotations.SerializedName;

public enum MessageType {
  @SerializedName("Elvaco MVP MQ Measurement Message 1.0")
  METERING_MEASUREMENT_V_1_0,
  @SerializedName("Elvaco MVP MQ Structure Message 1.0")
  METERING_METER_STRUCTURE_V_1_0
}
