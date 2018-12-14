package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class HibernateTsTzRangeMappingsTest extends IntegrationTest {

  @Test
  public void tstzrangeCanBeWrittenAndRead() {
    UUID meterId = randomUUID();
    PeriodRange activePeriod = PeriodRange.halfOpenFrom(
      ZonedDateTime.parse("2018-08-09T04:54:09.95Z"), null
    );
    physicalMeterJpaRepository.save(
      PhysicalMeterEntityMapper.toEntity(
        PhysicalMeter.builder()
          .organisationId(context().organisationId())
          .id(meterId)
          .address("123456789")
          .externalId("external-id")
          .medium("Some medium")
          .manufacturer("ELV")
          .activePeriod(activePeriod)
          .readIntervalMinutes(60)
          .build()
      )
    );

    assertThat(physicalMeterJpaRepository.findById(meterId).get().activePeriod)
      .isEqualTo(activePeriod);
  }

  @Test
  public void emptyTstzrangeCanBeWrittenAndRead() {
    UUID meterId = randomUUID();
    physicalMeterJpaRepository.save(
      PhysicalMeterEntityMapper.toEntity(
        PhysicalMeter.builder()
          .organisationId(context().organisationId())
          .id(meterId)
          .address("123456789")
          .externalId("external-id")
          .medium("Some medium")
          .manufacturer("ELV")
          .activePeriod(PeriodRange.empty())
          .readIntervalMinutes(60)
          .build()
      )
    );

    assertThat(physicalMeterJpaRepository.findById(meterId).get().activePeriod.isEmpty())
      .isTrue();
  }
}
