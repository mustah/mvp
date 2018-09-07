package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterEntityMapperTest {

  @Test
  public void mapToEntity() {
    PhysicalMeter physicalMeter = newPhysicalMeter();

    PhysicalMeterEntity entity = PhysicalMeterEntityMapper.toEntity(physicalMeter);

    assertThat(entity.address).isEqualTo("567890");
    assertThat(entity.externalId).isEqualTo("external-id");
    assertThat(entity.medium).isEqualTo("My Medium");
    assertThat(entity.manufacturer).isEqualTo("ELV");
  }

  @Test
  public void mapToDomainModel() {
    PhysicalMeter physicalMeter = newPhysicalMeter();
    PhysicalMeterEntity entity = PhysicalMeterEntityMapper.toEntity(physicalMeter);

    assertThat(PhysicalMeterEntityMapper.toDomainModel(entity)).isEqualTo(physicalMeter);
  }

  @Test
  public void mapToDomainModelWithAlarms_ShouldBeEmpty() {
    PhysicalMeterEntity entity = new PhysicalMeterEntity(
      UUID.randomUUID(),
      OrganisationEntityMapper.toEntity(ELVACO),
      "address",
      "abc",
      "Gas",
      "ELV",
      UUID.randomUUID(),
      0,
      emptySet(),
      emptySet()
    );

    PhysicalMeter physicalMeter = PhysicalMeterEntityMapper.toDomainModelWithAlarms(entity);

    assertThat(physicalMeter.alarms).isEmpty();
  }

  @Test
  public void mapToDomainModelWithAlarms_ShouldHaveAlarms() {
    UUID physicalMeterId = UUID.randomUUID();
    UUID logicalMeterId = UUID.randomUUID();
    ZonedDateTime now = ZonedDateTime.now();

    PhysicalMeterEntity entity = new PhysicalMeterEntity(
      physicalMeterId,
      OrganisationEntityMapper.toEntity(ELVACO),
      "address",
      "abc",
      "Gas",
      "ELV",
      logicalMeterId,
      0,
      emptySet(),
      singleton(MeterAlarmLogEntity.builder()
        .physicalMeterId(physicalMeterId)
        .mask(12)
        .description("test")
        .start(now)
        .lastSeen(now)
        .build())
    );

    PhysicalMeter physicalMeter = PhysicalMeterEntityMapper.toDomainModelWithAlarms(entity);

    assertThat(physicalMeter.alarms).containsExactly(
      AlarmLogEntry.builder()
        .entityId(physicalMeterId)
        .start(now)
        .lastSeen(now)
        .mask(12)
        .description("test")
        .build());
  }

  private static PhysicalMeter newPhysicalMeter() {
    return PhysicalMeter.builder()
      .address("567890")
      .externalId("external-id")
      .medium("My Medium")
      .manufacturer("ELV")
      .organisation(ELVACO)
      .readIntervalMinutes(15)
      .build();
  }
}
