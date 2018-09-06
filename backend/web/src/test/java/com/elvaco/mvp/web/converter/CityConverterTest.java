package com.elvaco.mvp.web.converter;

import com.elvaco.mvp.web.dto.geoservice.CityDto;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CityConverterTest {

  private CityConverter converter;

  @Before
  public void setUp() {
    converter = new CityConverter();
  }

  @Test
  public void transformsArgumentIntoCityDto() {
    assertThat(converter.convert("sweden,stockholm"))
      .isEqualTo(new CityDto("stockholm", "sweden"));
  }

  @Test
  public void treatsEverythingAfterFirstSeparatorAsCity() {
    assertThat(converter.convert("sweden,stockholm,blaha"))
      .isEqualTo(new CityDto("stockholm,blaha", "sweden"));
  }
}
