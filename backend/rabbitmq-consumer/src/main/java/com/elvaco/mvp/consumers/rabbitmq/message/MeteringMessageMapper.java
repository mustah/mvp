package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Quantity;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

final class MeteringMessageMapper {

  static final List<String> DISTRICT_HEATING_METER_QUANTITIES =
    unmodifiableList(new ArrayList<>(mapMeterQuantities().keySet()));

  static final Map<String, Quantity> METER_TO_MVP_QUANTITIES = mapMeterQuantities();

  static final Map<String, String> METERING_TO_MVP_UNITS = meteringUnitTranslationsMap();

  private MeteringMessageMapper() {}

  private static Map<String, Quantity> mapMeterQuantities() {
    Map<String, Quantity> map = new HashMap<>();
    map.put("Return temp.", Quantity.RETURN_TEMPERATURE);
    map.put("Difference temp.", Quantity.DIFFERENCE_TEMPERATURE);
    map.put("Flow temp.", Quantity.VOLUME_FLOW);
    map.put("Volume flow", Quantity.VOLUME);
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
