package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
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
    Location location = new Location(3.1, 2.1, 1.1);


    MeteringPoint meteringPoint = new MeteringPoint(
      id,
      status,
      location,
      null,
      null
    );

    final MapMarkerDto mapMarkerDtoActual = meteringPointMapper.toMapMarkerDto(meteringPoint);

    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = id;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.1;
    mapMarkerDtoExpected.status = new IdNamedDto();
    mapMarkerDtoExpected.status.name = status;
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    assertThat(mapMarkerDtoActual).isEqualTo(mapMarkerDtoExpected);

  }
}
