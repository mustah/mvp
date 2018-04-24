package com.elvaco.mvp.consumers.rabbitmq;

import java.time.LocalDateTime;
import java.util.List;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
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

@Transactional
public class MessageHandlerTest extends IntegrationTest {

  @Autowired
  MessageHandler messageHandler;

  @Autowired
  MeasurementJpaRepository measurementJpaRepository;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);
  }

  @Test
  public void lastReceivedDuplicateMeasurementIsUsed() {
    LocalDateTime when = LocalDateTime.now();
    messageHandler.handle(newMeasurementMessage(singletonList(newValueDto(when, 1.0))));
    messageHandler.handle(newMeasurementMessage(singletonList(newValueDto(when, 2.0))));

    assertThat(measurementJpaRepository.findAll()).hasSize(1);
    MeasurementEntity found = measurementJpaRepository.findAll().get(0);
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

    messageHandler.handle(measurementMessage);

    assertThat(measurementJpaRepository.findAll()).hasSize(1);
    MeasurementEntity found = measurementJpaRepository.findAll().get(0);
    assertThat(found.created.toLocalDateTime()).isEqualTo(when);
    assertThat(found.value.getValue()).isEqualTo(2.0);
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
    return new ValueDto(when, value, "kWh", "Energy");
  }
}
