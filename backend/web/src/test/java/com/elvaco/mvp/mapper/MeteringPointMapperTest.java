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
import org.modelmapper.config.Configuration.AccessLevel;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointMapperTest {

  private MeteringPointMapper mapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    mapper = new MeteringPointMapper(modelMapper);
  }

  @Test
  public void mapMeteringPointToMapMarkerDto() {
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = 1L;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.1;
    mapMarkerDtoExpected.status = new IdNamedDto("Ok");
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    MeteringPoint meteringPoint = new MeteringPoint(
      1L,
      "Ok",
      new Location(3.1, 2.1, 1.1),
      new Date(),
      null
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(meteringPoint);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }
}
