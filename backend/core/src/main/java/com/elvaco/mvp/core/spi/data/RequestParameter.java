package com.elvaco.mvp.core.spi.data;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RequestParameter {
  ADDRESS("address"),
  ALARM("alarm"),
  THRESHOLD_AFTER("after"),
  THRESHOLD_BEFORE("before"),
  CITY("city"),
  COLLECTION_AFTER("collectionAfter"),
  COLLECTION_BEFORE("collectionBefore"),
  FACILITY("facility"),
  GATEWAY_ID("gatewayId"),
  GATEWAY_SERIAL("gatewaySerial"),
  MANUFACTURER("manufacturer"),
  MEDIUM("medium"),
  REPORTED("reported"),
  LIMIT("limit"),
  LOGICAL_METER_ID("logicalMeterId"),
  ORGANISATION("organisation"),
  PHYSICAL_METER_ID("physicalMeterId"),
  QUANTITY("quantity"),
  RESOLUTION("resolution"),
  REPORT_AFTER("reportAfter"),
  REPORT_BEFORE("reportBefore"),
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
      .filter(parameter -> Objects.equals(parameter.name, name))
      .findAny()
      .orElse(null);
  }

  @Override
  public String toString() {
    return name == null ? this.name() : name;
  }
}
