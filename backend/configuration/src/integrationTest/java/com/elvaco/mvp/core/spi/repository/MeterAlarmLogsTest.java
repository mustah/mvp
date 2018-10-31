package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeterAlarmLogsTest extends IntegrationTest {

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @After
  public void tearDown() {
    meterAlarmLogJpaRepository.deleteAll();
  }

  @Test
  public void createNewAlarm() {
    PhysicalMeter physicalMeter = preparePhysicalMeter();
    ZonedDateTime start = ZonedDateTime.now();

    AlarmLogEntry entity = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(123)
      .start(start)
      .description("Low battery")
      .build();

    AlarmLogEntry saved = meterAlarmLogs.save(entity);

    assertThat(saved.getId()).isPositive();
    assertThat(saved.lastSeen).isEqualTo(start);
  }

  @Test
  public void createSameAlarmShouldThrowException() {
    PhysicalMeter physicalMeter = preparePhysicalMeter();

    ZonedDateTime now = ZonedDateTime.now();

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .start(now)
      .mask(123)
      .description("Low battery")
      .build());

    assertThatThrownBy(() -> meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .start(now)
      .mask(123)
      .description("Low battery")
      .build())
    ).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  public void saveSeveralAlarmsForTheSameMeter() {
    PhysicalMeter physicalMeter = preparePhysicalMeter();

    AlarmLogEntry entity1 = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(123)
      .start(ZonedDateTime.now())
      .description("Low battery")
      .build();
    AlarmLogEntry entity2 = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(21)
      .start(ZonedDateTime.now())
      .description("Api error")
      .build();

    meterAlarmLogs.save(asList(entity1, entity2));

    assertThat(meterAlarmLogJpaRepository.findAll()).hasSize(2);
  }

  @Test
  public void findActiveAlarmsBeforeDate() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime oneWeek = now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
    ZonedDateTime twoDays = now.minusDays(2).truncatedTo(ChronoUnit.DAYS);

    PhysicalMeter physicalMeter = preparePhysicalMeter();

    AlarmLogEntry notActive = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(8)
      .start(oneWeek)
      .stop(oneWeek.plusHours(1))
      .lastSeen(oneWeek)
      .description("Low battery")
      .build();
    AlarmLogEntry activeOneWeekAgo = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(16)
      .start(oneWeek)
      .lastSeen(oneWeek)
      .description("Low battery")
      .build();
    AlarmLogEntry activeTwoDaysAgo = AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(32)
      .start(twoDays)
      .lastSeen(twoDays)
      .description("Api error")
      .build();

    meterAlarmLogs.save(asList(activeTwoDaysAgo, notActive, activeOneWeekAgo));

    assertThat(meterAlarmLogJpaRepository.findActiveAlamsOlderThan(now)).hasSize(2);
    assertThat(meterAlarmLogJpaRepository.findActiveAlamsOlderThan(now.minusDays(3))).hasSize(1);
  }

  private PhysicalMeter preparePhysicalMeter() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("externalId")
      .organisationId(context().organisationId())
      .created(ZonedDateTime.now())
      .meterDefinition(MeterDefinition.GAS_METER)
      .build();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("123123")
      .externalId(logicalMeter.externalId)
      .medium(logicalMeter.getMedium())
      .manufacturer("ELV")
      .logicalMeterId(logicalMeter.getId())
      .readIntervalMinutes(15)
      .build();

    logicalMeters.save(logicalMeter);
    return physicalMeters.save(physicalMeter);
  }
}
