package com.elvaco.mvp.consumers.rabbitmq;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.AlarmMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.MeteringAlarmMessageConsumer;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterPk;
import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class AlarmMessageConsumerTest extends IntegrationTest {

  private static final LocalDateTime START = LocalDateTime.parse("2018-03-07T16:13:09");

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private PhysicalMeterUseCases physicalMeterUseCases;

  @Autowired
  private OrganisationUseCases organisationUseCases;

  private AlarmMessageConsumer meteringAlarmMessageConsumer;

  @Autowired
  private CacheManager cacheManager;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);

    meteringAlarmMessageConsumer = new MeteringAlarmMessageConsumer(
      physicalMeterUseCases,
      organisationUseCases,
      meterAlarmLogs
    );
  }

  @After
  public void tearDown() {
    cacheManager.getCacheNames().stream()
      .map(name -> cacheManager.getCache(name))
      .forEach(Cache::clear);

    meterAlarmLogJpaRepository.deleteAll();
  }

  @Transactional
  @Test
  public void addAlarm() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm = new AlarmDto(START, 1);

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();

    MeterAlarmLogEntity entity = alarms.get(0);
    ZoneId actualZoneId = entity.start.getZone();
    assertThat(alarms).containsExactly(MeterAlarmLogEntity.builder()
      .id(entity.id)
      .pk(new PhysicalMeterPk(physicalMeter.id, physicalMeter.organisationId))
      .mask(1)
      .start(zonedDateTimeOf(START, actualZoneId))
      .lastSeen(zonedDateTimeOf(START, actualZoneId))
      .build());
  }

  @Transactional
  @Test
  public void lastReceivedDuplicateAlarmIsUsed() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm1 = new AlarmDto(START, 42);
    AlarmDto alarm2 = new AlarmDto(START.plusHours(1), 42);

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm1));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm2));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();

    MeterAlarmLogEntity entity = alarms.get(0);
    ZoneId actualZoneId = entity.start.getZone();
    assertThat(alarms).extracting("pk.physicalMeterId").containsExactly(physicalMeter.id);
    assertThat(alarms).extracting("start").containsExactly(zonedDateTimeOf(START, actualZoneId));
  }

  @Transactional
  @Test
  public void useTheLastSeenTimestamp_InsteadOfUpdatingItWithTheLatest() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm1 = new AlarmDto(START, 42);
    AlarmDto alarm2 = new AlarmDto(START.plusHours(1), 42);
    AlarmDto alarm3 = new AlarmDto(START.minusHours(1), 42);

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm1));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm2));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm3));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();
    MeterAlarmLogEntity entity = alarms.get(0);
    ZoneId actualZoneId = entity.start.getZone();

    assertThat(alarms).extracting("pk.physicalMeterId").containsExactly(physicalMeter.id);

    assertThat(alarms).extracting("start")
      .containsExactly(zonedDateTimeOf(alarm3.timestamp, actualZoneId));

    assertThat(alarms).extracting("lastSeen")
      .containsExactly(zonedDateTimeOf(alarm2.timestamp, actualZoneId));
  }

  @Transactional
  @Test
  public void addAlarmsToSamePhysicalMeter() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm1 = new AlarmDto(START, 42);
    AlarmDto alarm2 = new AlarmDto(START.plusMinutes(5), 12);

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm1));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm2));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();
    ZoneId actualZoneId = alarms.get(0).start.getZone();

    assertThat(alarms).extracting(alarm -> tuple(
      alarm.pk.physicalMeterId,
      alarm.mask,
      alarm.start,
      alarm.lastSeen
    )).containsExactlyInAnyOrder(
      tuple(
        physicalMeter.id,
        42,
        zonedDateTimeOf(alarm1.timestamp, actualZoneId),
        zonedDateTimeOf(alarm1.timestamp, actualZoneId)
      ),
      tuple(
        physicalMeter.id,
        12,
        zonedDateTimeOf(alarm2.timestamp, actualZoneId),
        zonedDateTimeOf(alarm2.timestamp, actualZoneId)
      )
    );
  }

  private MeteringAlarmMessageDto newAlarmMessage(AlarmDto... alarms) {
    return new MeteringAlarmMessageDto(
      new IdDto("meter-123"),
      new IdDto("external-123"),
      context().defaultOrganisation().externalId,
      "Test source system",
      asList(alarms)
    );
  }

  private PhysicalMeter savePhysicalMeter() {
    return physicalMeterUseCases.save(PhysicalMeter.builder()
      .address("meter-123")
      .externalId("external-123")
      .organisationId(context().organisationId())
      .build());
  }

  private static ZonedDateTime zonedDateTimeOf(LocalDateTime timestamp, ZoneId actualZoneId) {
    return ZonedDateTime.of(timestamp, METERING_TIMEZONE).withZoneSameInstant(actualZoneId);
  }
}
