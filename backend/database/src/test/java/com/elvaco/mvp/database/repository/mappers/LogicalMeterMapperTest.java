package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.UUID;

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
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterMapperTest {

  @Test
  public void mapsPhysicalMeters() {
    LogicalMeter logicalMeter = new LogicalMeter(
      randomUUID(),
      "an-external-id",
      randomUUID(),
      MeterDefinition.DISTRICT_HEATING_METER,
      UNKNOWN_LOCATION,
      singletonList(PhysicalMeter.builder()
        .organisation(ELVACO)
        .address("1234")
        .externalId("an-external-ID")
        .medium("My medium")
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build())
    );

    LogicalMeterEntity logicalMeterEntity = LogicalMeterMapper.toEntity(logicalMeter);

    assertThat(logicalMeterEntity.physicalMeters).hasSize(1);
    assertThat(LogicalMeterMapper.toDomainModel(logicalMeterEntity)).isEqualTo(logicalMeter);
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

    LogicalMeter logicalMeter = LogicalMeterMapper.toDomainModel(logicalMeterEntity);

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
          singleton(new Quantity(
            1L,
            "Speed",
            new QuantityPresentationInformation("mps", SeriesDisplayMode.READOUT)
          )),
          false
        ),
        emptyList()
      )
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

    LogicalMeter logicalMeter = LogicalMeterMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      new LogicalMeter(
        meterId,
        "an-external-id",
        organisationId,
        UNKNOWN_LOCATION,
        created,
        emptyList(),
        new MeterDefinition(
          MeterDefinitionType.UNKNOWN_METER_TYPE,
          "My energy meter",
          singleton(new Quantity(
            1L,
            "Energy",
            new QuantityPresentationInformation("kWh", SeriesDisplayMode.READOUT)
          )),
          false
        ),
        emptyList()
      )
    );
  }

  @Test
  public void mapLogicalMeterDomainModelToEntity() {
    ZonedDateTime created = utcZonedDateTimeOf("2001-01-01T10:14:00.00Z");

    UUID meterId = randomUUID();
    LogicalMeterEntity logicalMeterEntityExpected = new LogicalMeterEntity(
      meterId,
      "an-external-id",
      randomUUID(),
      created,
      newMeterDefinitionEntity("Energy", "kWh", "Energy meter")
    );
    logicalMeterEntityExpected.location = new LocationEntity(meterId, 3.1, 2.1, 1.0);

    LogicalMeterEntity logicalMeterEntity = LogicalMeterMapper.toEntity(
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
          singleton(new Quantity(
            1L,
            "Energy",
            new QuantityPresentationInformation("kWh", SeriesDisplayMode.READOUT)
          )),
          false
        ),
        emptyList()
      ));

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
      singleton(new QuantityEntity(1L, quantityName, quantityUnit, SeriesDisplayMode.READOUT)),
      name,
      false
    );
  }
}
