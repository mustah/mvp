package com.elvaco.mvp.core.util;

import java.io.IOException;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Json {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Nullable
  public static JsonNode toJsonNode(Object object) {
    try {
      return OBJECT_MAPPER.readTree(toJson(object));
    } catch (IOException e) {
      log.warn("Unable to read object tree: {}", object, e);
      return null;
    }
  }

  @Nullable
  public static JsonNode toJsonNode(String json) {
    try {
      return OBJECT_MAPPER.readTree(json);
    } catch (IOException e) {
      log.warn("Unable to read object tree: {}", json, e);
      return null;
    }
  }

  @Nullable
  private static String toJson(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (IOException e) {
      log.warn("Unable to serialize object: {}", object, e);
      return null;
    }
  }

  @Nullable
  public static <T> T toObject(String s, Class<T> valueType) {
    try {
      return OBJECT_MAPPER.readValue(s, valueType);
    } catch (IOException e) {
      log.warn("Unable to deserialize string {} to object of type: {}", s, valueType, e);
      return null;
    }
  }

  @Nullable
  public static <T> T toObject(TreeNode node, Class<T> valueType) {
    try {
      return OBJECT_MAPPER.treeToValue(node, valueType);
    } catch (IOException e) {
      log.warn("Unable to deserialize string {} to object of type: {}", node, valueType, e);
      return null;
    }
  }
}
