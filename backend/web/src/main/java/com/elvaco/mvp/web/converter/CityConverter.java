package com.elvaco.mvp.web.converter;

import com.elvaco.mvp.web.dto.geoservice.CityDto;

import org.springframework.core.convert.converter.Converter;

public class CityConverter implements Converter<String, CityDto> {

  @Override
  public CityDto convert(String source) {
    String[] parts = source.split(",", 2);
    String country = parts[0].trim();
    String name = parts[1].trim();

    return new CityDto(name, country);
  }
}
