package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class MeteringMessageMapper {

  /**
   * Metering stores and treats all values as CET.
   * At least it's consistent!
   */
  public static final ZoneId METERING_TIMEZONE = ZoneId.of("UTC+1");

  static final Map<String, String> METERING_TO_MVP_UNITS = meteringUnitTranslationsMap();

  private static final Set<String> DISTRICT_HEATING_METER_QUANTITIES =
    unmodifiableSet(new HashSet<>(asList(
      "Return temp.",
      "Difference temp.",
      "Flow temp.",
      "Volume flow",
      "Power",
      "Volume",
      "Energy"
    )));

  private static final Map<String, Quantity> METER_TO_MVP_QUANTITIES = mapMeterQuantities();

  static MeterDefinition resolveMeterDefinition(List<ValueDto> values) {
    Set<String> quantities = values.stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toSet());

    if (quantities.equals(DISTRICT_HEATING_METER_QUANTITIES)) {
      return MeterDefinition.DISTRICT_HEATING_METER;
    }

    return MeterDefinition.UNKNOWN_METER;
  }

  static Optional<Quantity> mappedQuantity(String quantityName) {
    Quantity quantity = METER_TO_MVP_QUANTITIES.get(quantityName);
    return Optional.ofNullable(quantity);
  }

  static String mapToEvoMedium(String medium) {
    if ("Cold water".equals(medium)) {
      return Medium.WATER.medium;
    } else if ("Roomsensor".equals(medium)) {
      return Medium.ROOM_TEMP.medium;
    } else {
      return medium;
    }
  }

  private static Map<String, Quantity> mapMeterQuantities() {
    Map<String, Quantity> map = new HashMap<>();
    map.put("Return temp.", Quantity.RETURN_TEMPERATURE);
    map.put("Difference temp.", Quantity.DIFFERENCE_TEMPERATURE);
    map.put("Flow temp.", Quantity.FORWARD_TEMPERATURE);
    map.put("Volume flow", Quantity.VOLUME_FLOW);
    map.put("Power", Quantity.POWER);
    map.put("Volume", Quantity.VOLUME);
    map.put("Energy", Quantity.ENERGY);
    map.put("External temp", Quantity.EXTERNAL_TEMPERATURE);
    map.put("relative-humidity", Quantity.HUMIDITY);
    map.put("Energy return", Quantity.ENERGY_RETURN);
    map.put("Reactive energy", Quantity.REACTIVE_ENERGY);
    return unmodifiableMap(map);
  }

  private static Map<String, String> meteringUnitTranslationsMap() {
    Map<String, String> map = new HashMap<>();
    map.put("Celsius", "°C");
    map.put("Kelvin", "K");
    map.put("m3", "m³");
    map.put("m3/h", "m³/h");
    return unmodifiableMap(map);
  }
}
