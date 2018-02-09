package com.elvaco.mvp.web.mapper;

import java.util.Collections;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;

import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterMapperTest {

  private LogicalMeterMapper mapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    mapper = new LogicalMeterMapper(modelMapper);
  }

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = 1L;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.0;
    mapMarkerDtoExpected.status = new IdNamedDto("Ok");
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    Location location = new LocationBuilder()
      .coordinate(new GeoCoordinate(3.1, 2.1, 1.0))
      .build();

    LogicalMeter logicalMeter = new LogicalMeter(
      1L,
      "Ok",
      location,
      new Date(),
      null,
      Collections.emptyList(),
      null
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(logicalMeter);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }
}
