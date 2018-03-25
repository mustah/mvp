package com.elvaco.geoservice.repository.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JpaConverterJson implements AttributeConverter<Object, String> {

  private static final  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Object meta) {
    try {
      return OBJECT_MAPPER.writeValueAsString(meta);
    } catch (JsonProcessingException ex) {
      return null;
      // or throw an error
    }
  }

  @Override
  public Object convertToEntityAttribute(String dbData) {
    try {
      return OBJECT_MAPPER.readValue(dbData, Object.class);
    } catch (IOException ex) {
      // logger.error("Unexpected IOEx decoding json from database: " + dbData);
      return null;
    }
  }
}