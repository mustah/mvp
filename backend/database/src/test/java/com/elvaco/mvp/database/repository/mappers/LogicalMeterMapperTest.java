package com.elvaco.mvp.database.repository.mappers;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
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
      new PhysicalMeterMapper(new OrganisationMapper(), new MeterStatusLogMapper()),
      new GatewayMapper()
    );
  }

  @Test
  public void mapsPhysicalMeters() {
    LogicalMeter logicalMeter = new LogicalMeter(
      randomUUID(), "an-external-id",
      randomUUID(),
      MeterDefinition.DISTRICT_HEATING_METER,
      singletonList(new PhysicalMeter(ELVACO, "1234", "an-external-ID", "My medium", "ELV"))
    );

    LogicalMeterEntity logicalMeterEntity = logicalMeterMapper.toEntity(logicalMeter);

    assertThat(logicalMeterEntity.physicalMeters).hasSize(1);
    assertThat(logicalMeterMapper.toDomainModel(logicalMeterEntity)).isEqualTo(logicalMeter);
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelWithPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    UUID organisationId = randomUUID();

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      meterId,
      "an-external-id",
      organisationId,
      created,
      newMeterDefinitionEntity("Speed", "mps", "speed-o-meter")
    );
    logicalMeterEntity.setLocation(new LocationEntity(meterId, 3.1, 2.1, 1.0));

    LogicalMeter logicalMeter = logicalMeterMapper.toDomainModel(logicalMeterEntity);

    Location expectedLocation = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

    assertThat(expectedLocation.getCoordinate()).isEqualTo(logicalMeter.location.getCoordinate());

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        meterId,
        "an-external-id",
        organisationId,
        expectedLocation,
        created,
        emptyList(),
        new MeterDefinition(
          MeterDefinitionType.UNKNOWN_METER_TYPE,
          "speed-o-meter",
          singleton(new Quantity(1L, "Speed", "mps")),
          false
        ),
        emptyList(),
        emptyList()
      )
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelOutPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    UUID organisationId = randomUUID();

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntity =
      new LogicalMeterEntity(
        meterId,
        "an-external-id",
        organisationId,
        created,
        newMeterDefinitionEntity("Energy", "kWh", "My energy meter")
      );

    LogicalMeter logicalMeter = logicalMeterMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        meterId,
        "an-external-id",
        organisationId,
        Location.UNKNOWN_LOCATION,
        created,
        emptyList(),
        new MeterDefinition(
          MeterDefinitionType.UNKNOWN_METER_TYPE,
          "My energy meter",
          singleton(new Quantity(1L, "Energy", "kWh")),
          false
        ),
        emptyList(),
        emptyList()
      )
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity(
      meterId,
      "an-external-id",
      randomUUID(),
      created,
      newMeterDefinitionEntity("Energy", "kWh", "Energy meter")
    );
    logicalMeterEntityExpected.setLocation(new LocationEntity(meterId, 3.1, 2.1, 1.0));

    LogicalMeterEntity logicalMeterEntity = logicalMeterMapper.toEntity(
      new LogicalMeter(
        meterId,
        "an-external-id",
        randomUUID(),
        new LocationBuilder()
          .latitude(3.1)
          .longitude(2.1)
          .confidence(1.0)
          .build(),
        created,
        emptyList(),
        new MeterDefinition(
          MeterDefinitionType.UNKNOWN_METER_TYPE,
          "Energy meter",
          singleton(new Quantity(1L, "Energy", "kWh")),
          false
        ),
        emptyList(),
        emptyList()
      ));

    LocationEntity location = logicalMeterEntity.getLocation();
    assertThat(location.confidence).isEqualTo(1.0);
    assertThat(location.latitude).isEqualTo(3.1);
    assertThat(location.longitude).isEqualTo(2.1);
    assertThat(logicalMeterEntity).isEqualTo(logicalMeterEntityExpected);
  }

  private MeterDefinitionEntity newMeterDefinitionEntity(
    String quantityName,
    String quantityUnit,
    String name
  ) {
    return new MeterDefinitionEntity(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      singleton(new QuantityEntity(1L, quantityName, quantityUnit)),
      name,
      false
    );
  }
}
