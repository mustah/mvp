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
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;
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
import static org.junit.Assume.assumeTrue;

public class AlarmMessageConsumerTest extends RabbitIntegrationTest {

  private static final LocalDateTime START = LocalDateTime.parse("2018-03-07T16:13:09");
  private static final ZonedDateTime NOW = ZonedDateTime.now();

  @Autowired
  private MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private PhysicalMeterUseCases physicalMeterUseCases;

  @Autowired
  private OrganisationUseCases organisationUseCases;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  private AlarmMessageConsumer meteringAlarmMessageConsumer;

  @Autowired
  private CacheManager cacheManager;

  @Before
  public void setUp() {
    assumeTrue(isRabbitConnected());
    assumeTrue(isPostgresDialect());

    authenticate(context().superAdmin);

    meteringAlarmMessageConsumer = new MeteringAlarmMessageConsumer(
      physicalMeterUseCases,
      organisationUseCases,
      meterAlarmLogs,
      () -> NOW
    );
  }

  @After
  public void tearDown() {
    cacheManager.getCacheNames().stream()
      .map(name -> cacheManager.getCache(name))
      .forEach(Cache::clear);

    physicalMeterJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
  }

  @Transactional
  @Test
  public void addAlarm() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm = new AlarmDto(START, 1, "Test");

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();

    MeterAlarmLogEntity entity = alarms.get(0);
    ZoneId actualZoneId = entity.start.getZone();
    assertThat(alarms).containsExactly(MeterAlarmLogEntity.builder()
      .id(entity.id)
      .physicalMeterId(physicalMeter.id)
      .mask(1)
      .start(toZonedDateTime(actualZoneId))
      .lastSeen(toZonedDateTime(actualZoneId))
      .description("Test")
      .build());
  }

  @Transactional
  @Test
  public void lastReceivedDuplicateMeasurementIsUsed() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm = new AlarmDto(START, 42, "Low battery");

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();

    MeterAlarmLogEntity entity = alarms.get(0);
    ZoneId actualZoneId = entity.start.getZone();
    assertThat(alarms).containsExactly(MeterAlarmLogEntity.builder()
      .id(entity.id)
      .physicalMeterId(physicalMeter.id)
      .mask(42)
      .start(toZonedDateTime(actualZoneId))
      .lastSeen(toZonedDateTime(actualZoneId))
      .description("Low battery")
      .build());
  }

  @Transactional
  @Test
  public void addAlarmsToSamePhysicalMeter() {
    PhysicalMeter physicalMeter = savePhysicalMeter();
    AlarmDto alarm1 = new AlarmDto(START, 42, "Low battery");
    AlarmDto alarm2 = new AlarmDto(START.plusMinutes(5), 12, "Bad api");

    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm1));
    meteringAlarmMessageConsumer.accept(newAlarmMessage(alarm2));

    List<MeterAlarmLogEntity> alarms = meterAlarmLogJpaRepository.findAll();
    ZoneId actualZoneId = alarms.get(0).start.getZone();

    assertThat(alarms).extracting("physicalMeterId")
      .containsExactlyInAnyOrder(physicalMeter.id, physicalMeter.id);

    assertThat(alarms).extracting("description")
      .containsExactlyInAnyOrder("Low battery", "Bad api");

    assertThat(alarms).extracting("mask")
      .containsExactlyInAnyOrder(42, 12);

    assertThat(alarms).extracting("start").containsExactlyInAnyOrder(
      toZonedDateTime(actualZoneId),
      toZonedDateTime(actualZoneId).plusMinutes(5)
    );

    assertThat(alarms).extracting("lastSeen")
      .containsExactlyInAnyOrder(NOW, NOW);
  }

  private MeteringAlarmMessageDto newAlarmMessage(AlarmDto... alarms) {
    return new MeteringAlarmMessageDto(
      new MeterIdDto("meter-123"),
      new FacilityIdDto("external-123"),
      context().organisation().externalId,
      "Test source system",
      asList(alarms)
    );
  }

  private PhysicalMeter savePhysicalMeter() {
    return physicalMeterUseCases.save(PhysicalMeter.builder()
      .address("meter-123")
      .externalId("external-123")
      .organisation(context().organisation())
      .build());
  }

  private ZonedDateTime toZonedDateTime(ZoneId actualZone) {
    return ZonedDateTime.of(START, METERING_TIMEZONE).withZoneSameInstant(actualZone);
  }
}