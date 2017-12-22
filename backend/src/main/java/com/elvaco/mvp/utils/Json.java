package com.elvaco.mvp.utils;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Json {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(Include.NON_NULL);

  private Json() {}

  public static String toJson(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.warn("Unable to serialize object: {}", object, e);
      return null;
    }
  }

  public static JsonNode toJsonNode(Object object) {
    try {
      return OBJECT_MAPPER.readTree(toJson(object));
    } catch (IOException e) {
      log.warn("Unable to read object tree: {}", object, e);
      return null;
    }
  }
}
