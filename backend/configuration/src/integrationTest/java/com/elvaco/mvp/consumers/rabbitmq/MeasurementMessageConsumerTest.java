package com.elvaco.mvp.consumers.rabbitmq;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.tuple;

public class MeasurementMessageConsumerTest extends IntegrationTest {

  private static final String METER_ID_1 = "meterId-1";
  private static final String FACILITY = "facility";
  private static final String ORGANISATION_ID = "organisationId";
  private static final String SOURCE_SYSTEM_ID = "sourceSystemId";
  private static final String METER_ID_2 = "meterId-2";
  private static final String GATEWAY_SERIAL_1 = "A123";
  private static final ZonedDateTime CREATED = ZonedDateTime.of(
    LocalDateTime.parse("2018-03-07T16:13:09"),
    METERING_TIMEZONE
  );

  @Autowired
  private MeasurementMessageConsumer measurementMessageConsumer;

  @Autowired
  private CacheManager cacheManager;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);
  }

  @After
  public void tearDown() {
    cacheManager.getCacheNames().stream()
      .map(name -> cacheManager.getCache(name))
      .filter(Objects::nonNull)
      .forEach(Cache::clear);
  }

  @Transactional
  @Test
  public void lastReceivedDuplicateMeasurementIsUsed() {
    LocalDateTime when = CREATED.toLocalDateTime();
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(when, 1.0))));
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(when, 2.0))));

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).id.readoutTime).isEqualTo(CREATED);
    assertThat(all.get(0).value).isCloseTo(2.0, offset(0.1));
  }

  @Test
  @Transactional
  public void measurementsForDeletedMeterRecreatesMeter() {
    LocalDateTime when = CREATED.toLocalDateTime();
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(
      when,
      1.0
    ))));
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(
      when.plusHours(1),
      1.0
    ))));
    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(2);

    commitAndStartTransaction();

    logicalMeters.delete(logicalMeters.findAllBy(new RequestParametersAdapter()).get(0));

    commitAndStartTransaction();

    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(when, 2.0))));

    all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).id.readoutTime).isEqualTo(CREATED);
    assertThat(all.get(0).value).isCloseTo(2.0, offset(0.1));
  }

  @Test
  @Transactional
  public void meterReplacement_savingLogicalMeterSavesPhysicalMetersWithCorrectActivePeriod() {
    LocalDateTime when = CREATED.toLocalDateTime();

    // Given first meter without gateway
    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      null,
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      ORGANISATION_ID,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when, 1.0))
    ));

    // Meter replacement including gateway will save logicalMeter and gateway
    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_2),
      new IdDto(FACILITY),
      ORGANISATION_ID,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when.plusDays(1), 2.0))
    ));

    assertThat(measurementJpaRepository.findAll())
      .extracting(m -> m.getId().physicalMeter.address, m -> m.value)
      .containsExactlyInAnyOrder(tuple(METER_ID_1, 1.0), tuple(METER_ID_2, 2.0));
  }

  @Test
  @Transactional
  public void meterReplacement_testOverlappingPeriodConstraint() {
    LocalDateTime when = CREATED.toLocalDateTime();

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      null,
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      ORGANISATION_ID,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when, 1.0))
    ));

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      null,
      new IdDto(METER_ID_2),
      new IdDto(FACILITY),
      ORGANISATION_ID,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when.plusDays(1), 2.0))
    ));

    assertThatThrownBy(() ->
      measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
        null,
        new IdDto(METER_ID_2),
        new IdDto(FACILITY),
        ORGANISATION_ID,
        SOURCE_SYSTEM_ID,
        singletonList(newValueDto(when.minusDays(1), 3.0))
      )))
      .isInstanceOf(DataIntegrityViolationException.class)
      .hasStackTraceContaining("non_overlapping_active_periods");
  }

  @Transactional
  @Test
  public void duplicateMeasurementsInMessage_lastMeasurementInMessageIsUsed() {
    var when = CREATED.toLocalDateTime();
    var measurementMessage = newMeasurementMessage(asList(
      newValueDto(when, 1.0),
      newValueDto(when, 2.0)
    ));

    measurementMessageConsumer.accept(measurementMessage);

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).id.readoutTime).isEqualTo(CREATED);
    assertThat(all.get(0).value).isCloseTo(2.0, offset(0.1));
  }

  @Transactional
  @Test
  public void invalidUnitForQuantityIsDiscarded() {
    var measurementMessage = newMeasurementMessage(
      asList(
        new ValueDto(CREATED.toLocalDateTime(), 1.0, "m³", VOLUME.name),
        new ValueDto(CREATED.toLocalDateTime(), 2.0, "kWh", VOLUME.name),
        new ValueDto(CREATED.toLocalDateTime(), 3.0, "m³", ENERGY.name)
      ));

    measurementMessageConsumer.accept(measurementMessage);

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).id.quantity.name).isEqualTo("Volume");
    assertThat(all.get(0).value).isCloseTo(1.0, offset(0.1));
  }

  @Transactional
  @Test
  public void emptyUnitMeasurementIsDiscarded() {
    var measurementMessage = newMeasurementMessage(
      singletonList(new ValueDto(LocalDateTime.now(), 1.0, "", VOLUME.name))
    );

    measurementMessageConsumer.accept(measurementMessage);

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    assertThat(all).hasSize(0);
  }

  @Test
  public void emptyMeasurementMessageIsDiscarded() {
    var measurementMessage = newMeasurementMessage(emptyList());

    assertThatThrownBy(() -> measurementMessageConsumer.accept(measurementMessage))
      .isInstanceOf(IllegalArgumentException.class);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(List<ValueDto> values) {
    return new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      ORGANISATION_ID,
      SOURCE_SYSTEM_ID,
      values
    );
  }

  private ValueDto newValueDto(LocalDateTime when, double value) {
    return new ValueDto(when, value, "kWh", "Energy");
  }

  private static void commitAndStartTransaction() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
    TestTransaction.start();
  }
}
