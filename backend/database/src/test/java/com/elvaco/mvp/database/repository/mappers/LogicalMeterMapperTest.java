package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
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
      new MeterDefinitionMapper(),
      new LocationMapper(),
      new PhysicalMeterMapper(new OrganisationMapper())
    );
  }

  @Test
  public void mapsPhysicalMeters() {
    LogicalMeter logicalMeter = new LogicalMeter(
      MeterDefinition.DISTRICT_HEATING_METER,
      Collections.singletonList(
        new PhysicalMeter(ELVACO, "1234", "My medium", "ELV")
      )
    );
    LogicalMeterEntity logicalMeterEntity = logicalMeterMapper.toEntity(logicalMeter);
    assertThat(logicalMeterEntity.physicalMeters).hasSize(1);
    assertThat(logicalMeterMapper.toDomainModel(logicalMeterEntity)).isEqualTo(logicalMeter);
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelWithPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.latitude = 3.1;
    locationEntity.longitude = 2.1;
    locationEntity.confidence = 1.0;
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity();
    logicalMeterEntity.id = 1L;
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
        1L,
        "Ok",
        expectedLocation,
        created,
        new PropertyCollection(null),
        Collections.emptyList(),
        null
      )
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelOutPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity();
    logicalMeterEntity.id = 1L;
    logicalMeterEntity.status = "Ok";
    logicalMeterEntity.created = created;

    LogicalMeter logicalMeter = logicalMeterMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        1L,
        "Ok",
        new LocationBuilder().build(),
        created,
        new PropertyCollection(null),
        Collections.emptyList(),
        null
      )
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    LocationEntity locationEntityExpected = new LocationEntity();
    locationEntityExpected.confidence = 1.0;
    locationEntityExpected.longitude = 2.1;
    locationEntityExpected.latitude = 3.1;

    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity();
    logicalMeterEntityExpected.id = 1L;
    logicalMeterEntityExpected.status = "Ok";
    logicalMeterEntityExpected.created = created;
    logicalMeterEntityExpected.setLocation(locationEntityExpected);

    LogicalMeterEntity logicalMeterEntity = logicalMeterMapper.toEntity(
      new LogicalMeter(
        1L,
        "Ok",
        new LocationBuilder()
          .latitude(3.1)
          .longitude(2.1)
          .confidence(1.0)
          .build(),
        created,
        new PropertyCollection(null),
        Collections.emptyList(),
        null
      ));

    assertThat(logicalMeterEntity.getLocation()).isEqualTo(locationEntityExpected);
    assertThat(logicalMeterEntity).isEqualTo(logicalMeterEntityExpected);
  }
}
