package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterEntityMapperTest {

  @Before
  public void setUp() {
    QuantityAccess.singleton().loadAll(Quantity.QUANTITIES);
  }

  @Test
  public void mapsPhysicalMeters() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("an-external-id")
      .organisationId(ELVACO.id)
      .meterDefinition(MeterDefinition.DISTRICT_HEATING_METER)
      .physicalMeter(PhysicalMeter.builder()
        .organisation(ELVACO)
        .address("1234")
        .externalId("an-external-ID")
        .medium("My medium")
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build())
      .location(UNKNOWN_LOCATION)
      .build();

    LogicalMeterEntity logicalMeterEntity = LogicalMeterEntityMapper.toEntity(logicalMeter);

    assertThat(logicalMeterEntity.physicalMeters).hasSize(1);
    assertThat(LogicalMeterEntityMapper.toDomainModel(logicalMeterEntity)).isEqualTo(logicalMeter);
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelWithPosition() {
    ZonedDateTime created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z");

    UUID organisationId = randomUUID();

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      meterId,
      "an-external-id",
      organisationId,
      created,
      newMeterDefinitionEntity("Speed", "mps", "speed-o-meter")
    );
    logicalMeterEntity.location = new LocationEntity(meterId, 3.1, 2.1, 1.0);

    LogicalMeter logicalMeter = LogicalMeterEntityMapper.toDomainModel(logicalMeterEntity);

    Location expectedLocation = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

    assertThat(expectedLocation.getCoordinate()).isEqualTo(logicalMeter.location.getCoordinate());

    MeterDefinition meterDefinition = new MeterDefinition(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "speed-o-meter",
      singleton(new Quantity(
        1,
        "Speed",
        new QuantityPresentationInformation("mps", SeriesDisplayMode.READOUT)
      )),
      false
    );
    assertThat(logicalMeter).isEqualTo(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("an-external-id")
        .organisationId(organisationId)
        .created(created)
        .meterDefinition(meterDefinition)
        .location(expectedLocation)
        .build()
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelOutPosition() {
    ZonedDateTime created = ZonedDateTime.parse("2001-01-01T10:14:00.00Z");

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

    MeterDefinition meterDefinition = new MeterDefinition(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "My energy meter",
      singleton(new Quantity(
        1,
        "Energy",
        new QuantityPresentationInformation("kWh", SeriesDisplayMode.READOUT)
      )),
      false
    );

    LogicalMeter logicalMeter = LogicalMeterEntityMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("an-external-id")
        .organisationId(organisationId)
        .created(created)
        .meterDefinition(meterDefinition)
        .location(UNKNOWN_LOCATION)
        .build()
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    ZonedDateTime created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z");

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity(
      meterId,
      "an-external-id",
      ELVACO.id,
      created,
      newMeterDefinitionEntity("Energy", "kWh", "Energy meter")
    );
    logicalMeterEntityExpected.location = new LocationEntity(meterId, 3.1, 2.1, 1.0);

    MeterDefinition meterDefinition = new MeterDefinition(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "Energy meter",
      singleton(new Quantity(
        1,
        "Energy",
        new QuantityPresentationInformation("kWh", SeriesDisplayMode.READOUT)
      )),
      false
    );
    LogicalMeterEntity logicalMeterEntity = LogicalMeterEntityMapper.toEntity(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("an-external-id")
        .organisationId(ELVACO.id)
        .created(created)
        .meterDefinition(meterDefinition)
        .location(new LocationBuilder()
          .latitude(3.1)
          .longitude(2.1)
          .confidence(1.0)
          .build())
        .build());

    LocationEntity location = logicalMeterEntity.location;
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
      singleton(new QuantityEntity(1, quantityName, quantityUnit, SeriesDisplayMode.READOUT)),
      name,
      false
    );
  }
}
