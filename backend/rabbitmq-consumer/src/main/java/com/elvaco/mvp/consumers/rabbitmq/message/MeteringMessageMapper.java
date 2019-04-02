package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Quantity;

import lombok.experimental.UtilityClass;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class MeteringMessageMapper {

  /**
   * Metering stores and treats all values as CET.
   * At least it's consistent!
   */
  public static final ZoneId METERING_TIMEZONE = ZoneId.of("UTC+1");

  public static final Long DEFAULT_READ_INTERVAL_MINUTES = 60L;

  static final Map<String, String> METERING_TO_MVP_UNITS = Map.of(
    "Celsius", "°C",
    "Kelvin", "K",
    "m3", "m³",
    "m3/h", "m³/h"
  );

  private static final Set<String> DISTRICT_HEATING_METER_QUANTITIES = Set.of(
    "Return temp.",
    "Difference temp.",
    "Flow temp.",
    "Volume flow",
    "Power",
    "Volume",
    "Energy"
  );

  private static final Map<String, Quantity> METER_TO_MVP_QUANTITIES = Map.ofEntries(
    entry("Return temp.", Quantity.RETURN_TEMPERATURE),
    entry("Difference temp.", Quantity.DIFFERENCE_TEMPERATURE),
    entry("Flow temp.", Quantity.FORWARD_TEMPERATURE),
    entry("Volume flow", Quantity.VOLUME_FLOW),
    entry("Power", Quantity.POWER),
    entry("Volume", Quantity.VOLUME),
    entry("Energy", Quantity.ENERGY),
    entry("External temp", Quantity.EXTERNAL_TEMPERATURE),
    entry("relative-humidity", Quantity.HUMIDITY),
    entry("Energy return", Quantity.ENERGY_RETURN),
    entry("Reactive energy", Quantity.REACTIVE_ENERGY)
  );

  static String resolveMedium(List<ValueDto> values) {
    Set<String> quantities = values.stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toSet());

    if (quantities.equals(DISTRICT_HEATING_METER_QUANTITIES)) {
      return Medium.DISTRICT_HEATING;
    }
    return Medium.UNKNOWN_MEDIUM;
  }

  static Optional<Quantity> mappedQuantity(String quantityName) {
    Quantity quantity = METER_TO_MVP_QUANTITIES.get(quantityName);
    return Optional.ofNullable(quantity);
  }

  static Medium mapToEvoMedium(MediumProvider mediumProvider, @Nullable String medium) {
    if (medium == null) {
      return mediumProvider.getByNameOrThrow(Medium.UNKNOWN_MEDIUM);
    }
    Map<String, String> meteringToEvo = Map.of(
      "Cold water", Medium.WATER,
      "Roomsensor", Medium.ROOM_SENSOR,
      "Heat, Return temp", Medium.DISTRICT_HEATING,
      "Heat, Flow temp", Medium.DISTRICT_HEATING,
      "HeatCoolingLoadMeter", Medium.DISTRICT_HEATING,
      "HeatFlow Temp", Medium.DISTRICT_HEATING,
      "HeatReturn Temp", Medium.DISTRICT_HEATING,
      "Cooling load meter, flow temp", Medium.DISTRICT_COOLING,
      "Cooling load meter, Return temp", Medium.DISTRICT_COOLING
    );

    return mediumProvider
      .getByName(meteringToEvo.getOrDefault(medium, medium))
      .orElseGet(() -> mediumProvider.getByNameOrThrow(Medium.UNKNOWN_MEDIUM));
  }
}
