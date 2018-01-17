package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.IdNamedDto;
import com.elvaco.mvp.core.dto.MapMarkerDto;
import com.elvaco.mvp.core.dto.MapMarkerType;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointMapperTest {

  MeteringPointMapper meteringPointMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(Configuration.AccessLevel.PUBLIC);

    meteringPointMapper = new MeteringPointMapper(modelMapper);
  }

  @Test
  public void mapMeteringPointToMapMarkerDto() {
    Long id = 1L;
    String status = "Ok";
    double latitude = 3.1;
    double longitude = 2.1;
    double confidence = 1.1;


    MeteringPoint meteringPoint = new MeteringPoint(id, status, latitude, longitude, confidence);

    final MapMarkerDto mapMarkerDtoActual = meteringPointMapper.toMapMarkerDto(meteringPoint);

    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = id;
    mapMarkerDtoExpected.latitude = latitude;
    mapMarkerDtoExpected.longitude = longitude;
    mapMarkerDtoExpected.confidence = confidence;
    mapMarkerDtoExpected.status = new IdNamedDto();
    mapMarkerDtoExpected.status.name = status;
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    assertThat(mapMarkerDtoActual).isEqualTo(mapMarkerDtoExpected);

  }
}
