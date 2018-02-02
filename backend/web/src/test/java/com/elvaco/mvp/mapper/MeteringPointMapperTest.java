package com.elvaco.mvp.mapper;

import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointMapperTest {

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

    MapMarkerDto mapMarkerDto = new MeteringPointMapper().toMapMarkerDto(meteringPoint);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }
}
