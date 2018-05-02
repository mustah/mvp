package com.elvaco.mvp.consumers.rabbitmq;

import java.time.LocalDateTime;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assume.assumeTrue;

@Transactional
public class MeasurementMessageConsumerTest extends IntegrationTest {

  @Autowired
  private MeasurementMessageConsumer measurementMessageConsumer;

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);
  }

  @Test
  public void lastReceivedDuplicateMeasurementIsUsed() {
    LocalDateTime when = LocalDateTime.now();
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(when, 1.0))));
    measurementMessageConsumer.accept(newMeasurementMessage(singletonList(newValueDto(when, 2.0))));

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    MeasurementEntity found = all.get(0);
    assertThat(all).hasSize(1);
    assertThat(found.created.toLocalDateTime()).isEqualTo(when);
    assertThat(found.value.getValue()).isEqualTo(2.0);
  }

  @Test
  public void duplicateMeasurementsInMessage_lastMeasurementInMessageIsUsed() {
    LocalDateTime when = LocalDateTime.now();
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage(asList(
      newValueDto(when, 1.0),
      newValueDto(when, 2.0)
    ));

    measurementMessageConsumer.accept(measurementMessage);

    List<MeasurementEntity> all = measurementJpaRepository.findAll();
    MeasurementEntity found = all.get(0);
    assertThat(all).hasSize(1);
    assertThat(found.created.toLocalDateTime()).isEqualTo(when);
    assertThat(found.value.getValue()).isEqualTo(2.0);
  }

  @Test
  public void mixedDimensionsForMeterQuantity() {
    assumeTrue(isPostgresDialect());
    LocalDateTime when = LocalDateTime.now();
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage(asList(
      newValueDto(when, 2.0, "kWh"),
      newValueDto(when.plusMinutes(1), 1.0, "m³")
    ));

    assertThatThrownBy(() -> measurementMessageConsumer.accept(measurementMessage))
      .hasMessageContaining("Mixing dimensions for meter quantity is not allowed");
  }

  @Test
  public void emptyUnitMeasurementIsDiscarded() {
    MeteringMeasurementMessageDto measurementMessage = newMeasurementMessage(
      singletonList(new ValueDto(LocalDateTime.now(), 1.0, "", "Volume"))
    );

    assertThatThrownBy(() -> measurementMessageConsumer.accept(measurementMessage)).isInstanceOf(
      IllegalArgumentException.class);
  }

  private MeteringMeasurementMessageDto newMeasurementMessage(List<ValueDto> values) {
    return new MeteringMeasurementMessageDto(
      MessageType.METERING_MEASUREMENT_V_1_0,
      new GatewayIdDto("gateway-id"),
      new MeterIdDto("meter-id"),
      new FacilityIdDto("facility-id"),
      "organisation-id",
      "integration test source system",
      values
    );
  }

  private ValueDto newValueDto(LocalDateTime when, double value) {
    return newValueDto(when, value, "kWh");
  }

  private ValueDto newValueDto(LocalDateTime when, double value, String unit) {
    return new ValueDto(when, value, unit, "Energy");
  }
}
