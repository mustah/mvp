package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.Quantity.QUANTITIES;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterEntityMapperTest {

  private static final String TZ = "+01";

  private static final QuantityProvider QUANTITY_PROVIDER = name -> QUANTITIES.stream()
    .filter(quantity -> quantity.name.equals(name))
    .findAny()
    .orElse(null);

  private static final LogicalMeterEntityMapper logicalMeterEntityMapper =
    new LogicalMeterEntityMapper(
      new MeterDefinitionEntityMapper(
        new QuantityEntityMapper(QUANTITY_PROVIDER),
        QUANTITY_PROVIDER
      )
    );

  @Test
  public void mapsPhysicalMeters() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("an-external-id")
      .organisationId(ELVACO.id)
      .meterDefinition(MeterDefinition.DISTRICT_HEATING_METER)
      .physicalMeter(PhysicalMeter.builder()
        .organisationId(ELVACO.id)
        .address("1234")
        .externalId("an-external-ID")
        .medium("My medium")
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build())
      .location(UNKNOWN_LOCATION)
      .build();

    LogicalMeterEntity logicalMeterEntity = logicalMeterEntityMapper.toEntity(logicalMeter);

    assertThat(logicalMeterEntity.physicalMeters).hasSize(1);
    assertThat(logicalMeterEntityMapper.toDomainModel(logicalMeterEntity)).isEqualTo(logicalMeter);
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelWithPosition() {
    var created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z");
    var organisationId = randomUUID();
    var meterId = randomUUID();
    var pk = new EntityPk(meterId, organisationId);

    var logicalMeterEntity = new LogicalMeterEntity(
      pk,
      "an-external-id",
      created,
      newMeterDefinitionEntity("Speed", "kmh", "speed-o-meter"),
      TZ
    );

    logicalMeterEntity.location = LocationEntity.builder()
      .pk(pk)
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

    LogicalMeter logicalMeter = logicalMeterEntityMapper.toDomainModel(logicalMeterEntity);

    var expectedLocation = new LocationBuilder()
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

    assertThat(expectedLocation.getCoordinate()).isEqualTo(logicalMeter.location.getCoordinate());

    var meterDefinition = new MeterDefinition(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "speed-o-meter",
      singleton(new Quantity(
        1,
        "Speed",
        new QuantityPresentationInformation("kmh", SeriesDisplayMode.READOUT),
        "kmh"
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
        .utcOffset(TZ)
        .build()
    );
  }

  @Test
  public void mapLogicalMeterEntityToDomainModelOutPosition() {
    var created = ZonedDateTime.parse("2001-01-01T10:14:00.00Z");
    var organisationId = randomUUID();
    var meterId = randomUUID();

    var logicalMeterEntity =
      new LogicalMeterEntity(
        new EntityPk(meterId, organisationId), "an-external-id",
        created,
        newMeterDefinitionEntity("Energy", "kWh", "My energy meter"),
        TZ
      );

    MeterDefinition meterDefinition = new MeterDefinition(
      MeterDefinitionType.UNKNOWN_METER_TYPE,
      "My energy meter",
      singleton(new Quantity(
        1,
        "Energy",
        new QuantityPresentationInformation("kWh", SeriesDisplayMode.READOUT),
        "kWh"
      )),
      false
    );

    LogicalMeter logicalMeter = logicalMeterEntityMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("an-external-id")
        .organisationId(organisationId)
        .created(created)
        .meterDefinition(meterDefinition)
        .location(UNKNOWN_LOCATION)
        .utcOffset(TZ)
        .build()
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    ZonedDateTime created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z");

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity(
      new EntityPk(meterId, ELVACO.id), "an-external-id",
      created,
      newMeterDefinitionEntity("Energy", "kWh", "Energy meter"),
      TZ
    );
    logicalMeterEntityExpected.location = LocationEntity.builder()
      .pk(new EntityPk(meterId, ELVACO.id))
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

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
    LogicalMeterEntity logicalMeterEntity = logicalMeterEntityMapper.toEntity(
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
      singleton(new QuantityEntity(
        1,
        quantityName,
        quantityUnit,
        quantityUnit,
        SeriesDisplayMode.READOUT
      )),
      name,
      false
    );
  }
}
