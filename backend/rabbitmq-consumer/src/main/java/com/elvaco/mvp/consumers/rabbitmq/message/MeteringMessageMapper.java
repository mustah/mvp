package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.exception.UnknownQuantity;
import lombok.experimental.UtilityClass;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

@UtilityClass
class MeteringMessageMapper {

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
    Set<String> quantities = values
      .stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toSet());

    if (quantities.equals(DISTRICT_HEATING_METER_QUANTITIES)) {
      return MeterDefinition.DISTRICT_HEATING_METER;
    }

    return MeterDefinition.UNKNOWN_METER;
  }

  static String mappedQuantityName(String quantityName) {
    Quantity quantity = METER_TO_MVP_QUANTITIES.get(quantityName);
    if (quantity != null) {
      return quantity.name;
    } else {
      throw new UnknownQuantity(quantityName);
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
    map.put("Relative-humidity", Quantity.TEMPERATURE);
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
