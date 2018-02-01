package com.elvaco.mvp.repository.access;

import java.time.Instant;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.entity.meteringpoint.LocationEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
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

    meteringPointMapper = new MeteringPointMapper(
      new LocationMapper()
    );
  }

  @Test
  public void mapMeterPointEntityToDomainModelWithPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.latitude = 3.1;
    locationEntity.longitude = 2.1;
    locationEntity.confidence = 1.0;
    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = (long) 1;
    meteringPointEntity.status = "Ok";
    meteringPointEntity.created = created;
    meteringPointEntity.setLocation(locationEntity);

    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    Location expectedLocation = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();
    assertThat(expectedLocation.getCoordinate()).isEqualTo(meteringPoint.location.getCoordinate());

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        (long) 1,
        "Ok",
        expectedLocation,
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointEntityToDomainModelOutPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = (long) 1;
    meteringPointEntity.status = "Ok";
    meteringPointEntity.created = created;

    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        (long) 1,
        "Ok",
        new LocationBuilder().build(),
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointDomainModelToEntity() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Location location = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();
    final MeteringPoint meteringPoint = new MeteringPoint(
      (long) 1,
      "Ok",
      location,
      created,
      new PropertyCollection(null)
    );

    LocationEntity locationEntityExpected = new LocationEntity();
    locationEntityExpected.confidence = 1.0;
    locationEntityExpected.longitude = 2.1;
    locationEntityExpected.latitude = 3.1;

    MeteringPointEntity meteringPointEntityExpected = new MeteringPointEntity();
    meteringPointEntityExpected.id = (long) 1;
    meteringPointEntityExpected.status = "Ok";
    meteringPointEntityExpected.created = created;
    meteringPointEntityExpected.setLocation(locationEntityExpected);

    assertThat(meteringPointMapper.toEntity(meteringPoint).getLocation()).isEqualTo(
      locationEntityExpected);
    assertThat(meteringPointMapper.toEntity(meteringPoint)).isEqualTo(meteringPointEntityExpected);
  }
}
