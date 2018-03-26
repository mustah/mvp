package com.elvaco.geoservice.repository.converter;

import java.net.URI;

import javax.persistence.AttributeConverter;

import org.springframework.util.StringUtils;

public class JpaConverterUri implements AttributeConverter<URI, String> {

  @Override
  public String convertToDatabaseColumn(URI entityValue) {
    return (entityValue == null) ? null : entityValue.toString();
  }

  @Override
  public URI convertToEntityAttribute(String databaseValue) {
    return (StringUtils.hasLength(databaseValue) ? URI.create(databaseValue.trim()) : null);
  }
}
