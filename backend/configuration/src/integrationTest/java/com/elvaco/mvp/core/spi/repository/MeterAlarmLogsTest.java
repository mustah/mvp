package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

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
    var physicalMeter = preparePhysicalMeter();
    var start = ZonedDateTime.now();
    var entity = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
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
    var physicalMeter = preparePhysicalMeter();
    var now = ZonedDateTime.now();

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(now)
      .mask(123)
      .description("Low battery")
      .build());

    assertThatThrownBy(() -> meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .start(now)
      .mask(123)
      .description("Low battery")
      .build())
    ).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  public void saveSeveralAlarmsForTheSameMeter() {
    var physicalMeter = preparePhysicalMeter();

    var entity1 = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(123)
      .start(ZonedDateTime.now())
      .description("Low battery")
      .build();
    var entity2 = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(21)
      .start(ZonedDateTime.now())
      .description("Api error")
      .build();

    meterAlarmLogs.save(List.of(entity1, entity2));

    assertThat(meterAlarmLogJpaRepository.findAll()).hasSize(2);
  }

  @Test
  public void findActiveAlarmsBeforeDate() {
    var now = ZonedDateTime.now();
    var oneWeek = now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
    var twoDays = now.minusDays(2).truncatedTo(ChronoUnit.DAYS);

    var physicalMeter = preparePhysicalMeter();

    AlarmLogEntry notActive = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(8)
      .start(oneWeek)
      .stop(oneWeek.plusHours(1))
      .lastSeen(oneWeek)
      .description("Low battery")
      .build();
    AlarmLogEntry activeOneWeekAgo = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(16)
      .start(oneWeek)
      .lastSeen(oneWeek)
      .description("Low battery")
      .build();
    AlarmLogEntry activeTwoDaysAgo = AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(32)
      .start(twoDays)
      .lastSeen(twoDays)
      .description("Api error")
      .build();

    meterAlarmLogs.save(List.of(activeTwoDaysAgo, notActive, activeOneWeekAgo));

    assertThat(meterAlarmLogJpaRepository.findActiveAlarmsOlderThan(now)).hasSize(2);
    assertThat(meterAlarmLogJpaRepository.findActiveAlarmsOlderThan(now.minusDays(3))).hasSize(1);
  }

  private PhysicalMeter preparePhysicalMeter() {
    LogicalMeter logicalMeter = LogicalMeter.builder()
      .externalId("externalId")
      .organisationId(context().organisationId())
      .created(ZonedDateTime.now())
      .meterDefinition(MeterDefinition.DEFAULT_GAS)
      .build();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisationId(context().organisationId())
      .address("123123")
      .externalId(logicalMeter.externalId)
      .medium(logicalMeter.getMedium().name)
      .manufacturer("ELV")
      .logicalMeterId(logicalMeter.getId())
      .readIntervalMinutes(15)
      .build();

    logicalMeters.save(logicalMeter);
    return physicalMeters.save(physicalMeter);
  }
}
