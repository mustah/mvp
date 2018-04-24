package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

@UtilityClass
class MeteringMessageMapper {

  static final Map<String, String> METERING_TO_MVP_UNITS = meteringUnitTranslationsMap();

  private static final List<String> DISTRICT_HEATING_METER_QUANTITIES =
    unmodifiableList(new ArrayList<>(mapMeterQuantities().keySet()));

  private static final Map<String, Quantity> METER_TO_MVP_QUANTITIES = mapMeterQuantities();

  static MeterDefinition resolveMeterDefinition(List<ValueDto> values) {
    boolean isDistrictHeatingMeter = values
      .stream()
      .map(valueDto -> valueDto.quantity)
      .collect(toList())
      .containsAll(DISTRICT_HEATING_METER_QUANTITIES);
    return isDistrictHeatingMeter
      ? MeterDefinition.DISTRICT_HEATING_METER
      : MeterDefinition.UNKNOWN_METER;
  }

  static String mappedQuantityName(String quantity) {
    return METER_TO_MVP_QUANTITIES.getOrDefault(quantity, new Quantity(quantity)).name;
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
