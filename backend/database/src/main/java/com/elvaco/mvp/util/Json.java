package com.elvaco.mvp.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Json {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(Include.NON_NULL);

  private Json() {}

  public static JsonNode toJsonNode(Object object) {
    try {
      return OBJECT_MAPPER.readTree(toJson(object));
    } catch (IOException e) {
      log.warn("Unable to read object tree: {}", object, e);
      return null;
    }
  }

  private static String toJson(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (IOException e) {
      log.warn("Unable to serialize object: {}", object, e);
      return null;
    }
  }

  public static <T> T toObject(String s, Class<T> valueType) {
    try {
      return OBJECT_MAPPER.readValue(s, valueType);
    } catch (IOException e) {
      log.warn("Unable to deserialize string {} to object of type: {}", s, valueType, e);
      return null;
    }

  }
}
