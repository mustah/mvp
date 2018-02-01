package com.elvaco.mvp.mapper;

import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.modelmapper.config.Configuration.AccessLevel;

public class MeteringPointMapperTest {

  private MeteringPointMapper meteringPointMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    meteringPointMapper = new MeteringPointMapper(modelMapper);
  }

  @Test
  public void mapMeteringPointToMapMarkerDto() {
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = 1L;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.1;
    mapMarkerDtoExpected.status = new IdNamedDto();
    mapMarkerDtoExpected.status.name = "Ok";
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    MeteringPoint meteringPoint = new MeteringPoint(
      1L,
      "Ok",
      new Location(3.1, 2.1, 1.1),
      new Date(),
      null
    );

    assertThat(meteringPointMapper.toMapMarkerDto(meteringPoint)).isEqualTo(mapMarkerDtoExpected);
  }
}
