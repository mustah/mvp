package com.elvaco.mvp.dialect.function.h2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.measure.Quantity;
import javax.measure.Unit;

import com.elvaco.mvp.entity.measurement.MeasurementUnit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

@Slf4j
public final class CompatibilityFunctions {

  private static final Map<String, Unit<?>> CUSTOM_TYPES = new HashMap<>();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  static {
    SimpleUnitFormat.getInstance().alias(Units.CELSIUS, "Celsius");
    SimpleUnitFormat.getInstance().alias(Units.KELVIN, "Kelvin");
    /* Necessary hack, because UOM's unit parser doesn't approve of
    this unit format.*/
    CUSTOM_TYPES.put("m3", Units.CUBIC_METRE);
  }

  private CompatibilityFunctions() {
  }

  public static String unitAt(String valueAndUnit, String target) {
    Unit targetUnit = SimpleUnitFormat.getInstance().parse(target);

    Quantity<?> sourceQuantity;
    try {
      sourceQuantity = Quantities.getQuantity(valueAndUnit);
    } catch (IllegalArgumentException iex) {
      MeasurementUnit measurementUnit = new MeasurementUnit(valueAndUnit);
      if (CUSTOM_TYPES.containsKey(measurementUnit.getUnit())) {
        sourceQuantity = Quantities.getQuantity(
          measurementUnit.getValue(),
          CUSTOM_TYPES.get(measurementUnit.getUnit())
        );
      } else {
        throw iex;
      }
    }
    Quantity<?> resultQuantity = sourceQuantity.to(targetUnit);

    return new MeasurementUnit(
      resultQuantity.getUnit().toString(),
      resultQuantity.getValue().doubleValue()
    ).toString();
  }

  public static Boolean jsonbContains(String lhs, String rhs) {
    try {
      JsonNode lhsJson = OBJECT_MAPPER.readTree(lhs);
      JsonNode rhsJson = OBJECT_MAPPER.readTree(rhs);
      return jsonNodeContains(lhsJson, rhsJson);
    } catch (IOException ex) {
      log.error(
        String.format("Failed to convert string to json: left: %s right: %s", lhs, rhs),
        ex);
      return false;
    }
  }

  public static Boolean jsonbExists(String lhs, String rhs) {
    try {
      JsonNode lhsJson = OBJECT_MAPPER.readTree(lhs);
      return jsonNodeExists(lhsJson, rhs);
    } catch (IOException ex) {
      log.error(
        String.format("Failed to convert string to json: left: %s right: %s", lhs, rhs), ex);
      return false;
    }
  }

  private static boolean jsonNodeExists(JsonNode left, String key) {
    if (left.isTextual()) {
      return left.textValue().equals(key);
    } else if (left.isArray()) {
      return jsonNodeExistsFieldInArray((ArrayNode) left, key);
    } else if (left.isObject()) {
      return jsonNodeExistsFieldInObject((ObjectNode) left, key);
    }

    throw new UnsupportedOperationException(
      "left type: " + left.getNodeType().toString()
    );
  }

  private static boolean jsonNodeExistsFieldInObject(ObjectNode left, String key) {
    return left.has(key);
  }


  private static boolean jsonNodeExistsFieldInArray(ArrayNode array, String key) {
    for (JsonNode x : array) {
      if (x.isTextual() && x.textValue().equals(key)) {
        return true;
      }
    }
    return false;
  }

  private static boolean jsonContainerContains(JsonNode container, JsonNode contained) {
    assert container.isObject() || container.isArray();
    for (JsonNode el : contained) {
      boolean foundContainer = false;
      for (JsonNode containingElement : container) {
        if (containingElement.equals(el)
          || (el.isContainerNode() && jsonNodeContains(containingElement, el))) {
          foundContainer = true;
          break;
        }
      }
      if (!foundContainer) {
        return false;
      }
    }
    return true;
  }

  private static boolean jsonNodeContains(JsonNode lhs, JsonNode rhs) {
    if (lhs.isValueNode()) {
      return lhs.equals(rhs);
    } else if (lhs.isArray()) {
      return jsonContainerContains(lhs, rhs);
    } else if (lhs.isObject()) {
      if (!rhs.isObject()) {
        return false;
      }
      return jsonContainerContains(lhs, rhs);
    }

    throw new UnsupportedOperationException(
      "left type: " + lhs.getNodeType().toString()
        + ", right type:" + rhs.getNodeType().toString()
    );
  }
}
