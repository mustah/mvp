package com.elvaco.mvp.core.spi.data;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RequestParameter {
  ADDRESS("address"),
  AFTER("after"),
  ALARM("alarm"),
  BEFORE("before"),
  CITY("city"),
  FACILITY("facility"),
  GATEWAY_ID("gatewayId"),
  GATEWAY_SERIAL("gatewaySerial"),
  ID("id"),
  MANUFACTURER("manufacturer"),
  MAX_VALUE("maxValue"),
  MEDIUM("medium"),
  REPORTED("reported"),
  MIN_VALUE("minValue"),
  LOGICAL_METER_ID("logicalMeterId"),
  ORGANISATION("organisation"),
  PHYSICAL_METER_ID("physicalMeterId"),
  QUANTITY("quantity"),
  RESOLUTION("resolution"),
  SECONDARY_ADDRESS("secondaryAddress"),
  SERIAL("serial"),
  SORT("sort"),
  STATUS("status"),
  Q("q"),
  Q_FACILITY(null),
  Q_ADDRESS(null),
  Q_ORGANISATION(null),
  Q_SECONDARY_ADDRESS(null),
  Q_SERIAL(null),
  Q_CITY(null),
  WILDCARD("w");

  private final String name;

  @Nullable
  public static RequestParameter from(String name) {
    return Stream.of(values())
      .filter(parameter -> parameter.name != null)
      .filter(parameter -> parameter.name.equals(name))
      .findAny()
      .orElse(null);
  }

  @Override
  public String toString() {
    return name == null ? this.name() : name;
  }
}
