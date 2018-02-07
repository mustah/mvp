package com.elvaco.mvp.repository.mappers;

import java.time.Instant;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.entity.meter.LocationEntity;
import com.elvaco.mvp.entity.meter.LogicalMeterEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.modelmapper.config.Configuration.AccessLevel;

public class LogicalMeterMapperTest {

  private LogicalMeterMapper logicalMeterMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    logicalMeterMapper = new LogicalMeterMapper(
      new LocationMapper()
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelWithPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.latitude = 3.1;
    locationEntity.longitude = 2.1;
    locationEntity.confidence = 1.0;
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity();
    logicalMeterEntity.id = (long) 1;
    logicalMeterEntity.status = "Ok";
    logicalMeterEntity.created = created;
    logicalMeterEntity.setLocation(locationEntity);

    LogicalMeter logicalMeter = logicalMeterMapper.toDomainModel(logicalMeterEntity);

    Location expectedLocation = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();
    assertThat(expectedLocation.getCoordinate()).isEqualTo(logicalMeter.location.getCoordinate());

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        (long) 1,
        "Ok",
        expectedLocation,
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelOutPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity();
    logicalMeterEntity.id = (long) 1;
    logicalMeterEntity.status = "Ok";
    logicalMeterEntity.created = created;

    LogicalMeter logicalMeter = logicalMeterMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        (long) 1,
        "Ok",
        new LocationBuilder().build(),
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Location location = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();
    final LogicalMeter logicalMeter = new LogicalMeter(
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

    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity();
    logicalMeterEntityExpected.id = (long) 1;
    logicalMeterEntityExpected.status = "Ok";
    logicalMeterEntityExpected.created = created;
    logicalMeterEntityExpected.setLocation(locationEntityExpected);

    assertThat(logicalMeterMapper.toEntity(logicalMeter).getLocation()).isEqualTo(
      locationEntityExpected);
    assertThat(logicalMeterMapper.toEntity(logicalMeter)).isEqualTo(logicalMeterEntityExpected);
  }
}
