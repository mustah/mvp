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
  COLLECTION_AFTER("collectionAfter"),
  COLLECTION_BEFORE("collectionBefore"),
  FACILITY("facility"),
  GATEWAY_ID("gatewayId"),
  GATEWAY_SERIAL("gatewaySerial"),
  MANUFACTURER("manufacturer"),
  MEDIUM("medium"),
  REPORTED("reported"),
  LOGICAL_METER_ID("logicalMeterId"),
  ORGANISATION("organisation"),
  PHYSICAL_METER_ID("physicalMeterId"),
  QUANTITY("quantity"),
  RESOLUTION("resolution"),
  SECONDARY_ADDRESS("secondaryAddress"),
  SERIAL("serial"),
  SORT("sort"),
  STATUS("status"),
  THRESHOLD("threshold"),
  Q("q"),
  Q_FACILITY(),
  Q_ADDRESS(),
  Q_ORGANISATION(),
  Q_SECONDARY_ADDRESS(),
  Q_SERIAL(),
  Q_CITY(),
  WILDCARD("w");

  private final String name;

  RequestParameter() {
    this(null);
  }

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
