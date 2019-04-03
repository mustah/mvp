package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityEntity;
import com.elvaco.mvp.database.entity.meter.DisplayQuantityPk;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MediumEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.QUANTITIES;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.util.DateHelper.utcZonedDateTimeOf;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterEntityMapperTest extends DefaultTestFixture {

  private static final Medium UNKNOWN_MEDIUM = new Medium(
    null,
    Medium.UNKNOWN_MEDIUM
  );

  private static final QuantityProvider QUANTITY_PROVIDER = name -> QUANTITIES.stream()
    .filter(quantity -> quantity.name.equals(name))
    .findAny();

  private static final SystemMeterDefinitionProvider METER_DEFINITION_PROVIDER =
    medium -> Optional.of(MeterDefinition.UNKNOWN);

  private static final MediumProvider MEDIUM_PROVIDER = name -> Optional.of(UNKNOWN_MEDIUM);

  private static final LogicalMeterEntityMapper logicalMeterEntityMapper =
    new LogicalMeterEntityMapper(
      new MeterDefinitionEntityMapper(
        new MediumEntityMapper(MEDIUM_PROVIDER),
        new DisplayQuantityEntityMapper(new QuantityEntityMapper(QUANTITY_PROVIDER))
      ),
      METER_DEFINITION_PROVIDER,
      MEDIUM_PROVIDER,
      (manufacturer, deviceType, firmwareRevision, mask) -> Optional.empty()
    );

  @Test
  public void noMappingOfPhysicalMeters() {
    LogicalMeter logicalMeter = logicalMeter()
      .meterDefinition(MeterDefinition.UNKNOWN)
      .physicalMeter(physicalMeter().build())
      .build();

    LogicalMeterEntity logicalMeterEntity = logicalMeterEntityMapper.toEntity(logicalMeter);

    assertThat(logicalMeterEntity.physicalMeters).hasSize(0);
    assertThat(logicalMeterEntityMapper.toDomainModel(logicalMeterEntity))
      .isEqualTo(logicalMeter.toBuilder().clearPhysicalMeters().build());
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
      LogicalMeter.UTC_OFFSET
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
      0L,
      null,
      "speed-o-meter",
      UNKNOWN_MEDIUM,
      false,
      singleton(
        new DisplayQuantity(new Quantity(1, "Speed", "kmh"), DisplayMode.READOUT, "kmh")

      )
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
    var created = ZonedDateTime.parse("2001-01-01T10:14:00.00Z");
    var organisationId = randomUUID();
    var meterId = randomUUID();

    var logicalMeterEntity =
      new LogicalMeterEntity(
        new EntityPk(meterId, organisationId), "an-external-id",
        created,
        newMeterDefinitionEntity("Energy", "kWh", "My energy meter"),
        LogicalMeter.UTC_OFFSET
      );

    MeterDefinition meterDefinition =
      new MeterDefinition(
        0L,
        null,
        "My energy meter",
        UNKNOWN_MEDIUM,
        false,
        singleton(
          new DisplayQuantity(new Quantity(1, "Energy", "kWh"), DisplayMode.READOUT, "kWh")

        )
      );

    LogicalMeter logicalMeter = logicalMeterEntityMapper.toDomainModel(logicalMeterEntity);

    assertThat(logicalMeter).isEqualTo(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("an-external-id")
        .organisationId(organisationId)
        .created(created)
        .meterDefinition(meterDefinition)
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
      LogicalMeter.UTC_OFFSET
    );
    logicalMeterEntityExpected.location = LocationEntity.builder()
      .pk(new EntityPk(meterId, ELVACO.id))
      .latitude(3.1)
      .longitude(2.1)
      .confidence(1.0)
      .build();

    MeterDefinition meterDefinition =
      new MeterDefinition(
        0L,
        null,
        "Energy meter",
        UNKNOWN_MEDIUM,
        false,
        singleton(
          new DisplayQuantity(new Quantity(null, "Energy", "kWh"), DisplayMode.READOUT, "kWh")

        )
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
      0L,
      null,
      singleton(new DisplayQuantityEntity(
        new DisplayQuantityPk(
          new QuantityEntity(1, quantityName, quantityUnit), 0L, DisplayMode.READOUT
        ),
        quantityUnit,
        3
      )),
      name,
      new MediumEntity(UNKNOWN_MEDIUM.id, UNKNOWN_MEDIUM.name),
      false
    );
  }
}
