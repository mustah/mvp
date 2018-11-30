package com.elvaco.mvp.database.dialect.function.h2;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;

@SuppressWarnings("WeakerAccess") // Used by H2 DB
@Slf4j
@UtilityClass
public final class CompatibilityFunctions {

  @SuppressWarnings("WeakerAccess") // It's used in h2 provisioning loader
  public static Boolean jsonbContains(String lhs, String rhs) {
    try {
      JsonNode lhsJson = OBJECT_MAPPER.readTree(lhs);
      JsonNode rhsJson = OBJECT_MAPPER.readTree(rhs);
      return jsonNodeContains(lhsJson, rhsJson);
    } catch (IOException ex) {
      logJsonError(lhs, rhs, ex);
      return false;
    }
  }

  @SuppressWarnings("WeakerAccess") // It's used in h2 provisioning loader
  public static Boolean jsonbExists(String lhs, String rhs) {
    try {
      JsonNode lhsJson = OBJECT_MAPPER.readTree(lhs);
      return jsonNodeExists(lhsJson, rhs);
    } catch (IOException ex) {
      logJsonError(lhs, rhs, ex);
      return false;
    }
  }

  private static void logJsonError(String lhs, String rhs, IOException ex) {
    log.error(String.format("Failed to convert string to json: left: %s right: %s", lhs, rhs), ex);
  }

  private static boolean jsonNodeExists(JsonNode left, String key) {
    if (left.isTextual()) {
      return left.textValue().equals(key);
    } else if (left.isArray()) {
      return jsonNodeExistsFieldInArray((ArrayNode) left, key);
    } else if (left.isObject()) {
      return jsonNodeExistsFieldInObject((ObjectNode) left, key);
    }
    throw new UnsupportedOperationException("left type: " + left.getNodeType().toString());
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
      return rhs.isObject() && jsonContainerContains(lhs, rhs);
    }

    throw new UnsupportedOperationException(
      "left type: " + lhs.getNodeType().toString()
        + ", right type:" + rhs.getNodeType().toString()
    );
  }
}
