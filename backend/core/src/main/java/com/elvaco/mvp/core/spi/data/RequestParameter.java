package com.elvaco.mvp.core.spi.data;

import java.util.Arrays;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RequestParameter {
  ADDRESS("address"),
  AFTER("after"),
  BEFORE("before"),
  CITY("city"),
  FACILITY("facility"),
  GATEWAY_ID("gatewayId"),
  GATEWAY_SERIAL("gatewaySerial"),
  GATEWAY_STATUS("gatewayStatus"),
  ID("id"),
  MANUFACTURER("manufacturer"),
  MAX_VALUE("maxValue"),
  MEDIUM("medium"),
  METER_STATUS("meterStatus"),
  MIN_VALUE("minValue"),
  ORGANISATION("organisation"),
  PHYSICAL_METER_ID("physicalMeterId"),
  QUANTITY("quantity"),
  SECONDARY_ADDRESS("secondaryAddress"),
  SERIAL("serial"),
  SORT("sort"),
  STATUS("status");

  private final String name;

  @Nullable
  public static RequestParameter from(String name) {
    return Arrays.stream(values())
      .filter(parameter -> parameter.name.equals(name))
      .findAny()
      .orElse(null);
  }

  @Override
  public String toString() {
    return name;
  }
}
