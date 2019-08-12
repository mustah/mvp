package com.elvaco.mvp.consumers.rabbitmq;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MeasurementMessageConsumer;
import com.elvaco.mvp.producers.rabbitmq.dto.IdDto;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class MeasurementMessageConsumerGatewayTest extends IntegrationTest {

  private static final String METER_ID_1 = "meterId-1";
  private static final String FACILITY = "facility";
  private static final String SOURCE_SYSTEM_ID = "sourceSystemId";
  private static final String GATEWAY_SERIAL_1 = "A123";
  private static final String GATEWAY_SERIAL_2 = "B456";
  private static final ZonedDateTime CREATED = ZonedDateTime.of(
    LocalDateTime.parse("2018-03-07T16:13:09"),
    METERING_TIMEZONE
  );

  @Autowired
  private MeasurementMessageConsumer measurementMessageConsumer;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);
  }

  @Test
  @Transactional
  public void gatewayMeterIsUpdatedWithNewGateway() {
    LocalDateTime when = CREATED.toLocalDateTime();

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when, 1.0))
    ));

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_2),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when.plusDays(1), 2.0))
    ));

    assertThat(gatewayJpaRepository.findAll())
      .hasSize(2)
      .extracting(g -> g.serial)
      .containsExactlyInAnyOrder(GATEWAY_SERIAL_1, GATEWAY_SERIAL_2);

    var logicalMeter = logicalMeterJpaRepository.findBy(context().organisationId(), FACILITY);
    assertThat(logicalMeter).isPresent();

    assertThat(gatewaysMetersJpaRepository.findByLogicalMeterIdAndOrganisationId(
      logicalMeter.get().pk.id,
      context().organisationId()
    ))
      .extracting(gme -> gme.gateway.serial, gme -> gme.lastSeen.toInstant())
      .containsExactlyInAnyOrder(
        tuple(GATEWAY_SERIAL_1, CREATED.toInstant()),
        tuple(GATEWAY_SERIAL_2, CREATED.plusDays(1).toInstant())
      );
  }

  @Test
  @Transactional
  public void gatewayMeterIsUpdatedWithDate() {
    LocalDateTime when = CREATED.toLocalDateTime();

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when, 7.0))
    ));

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when.plusDays(1), 8.0))
    ));

    assertThat(gatewayJpaRepository.findAll())
      .hasSize(1)
      .extracting(g -> g.serial)
      .containsExactlyInAnyOrder(GATEWAY_SERIAL_1);

    var logicalMeter = logicalMeterJpaRepository.findBy(context().organisationId(), FACILITY);
    assertThat(logicalMeter).isPresent();

    commitTransaction();

    assertThat(gatewaysMetersJpaRepository.findByLogicalMeterIdAndOrganisationId(
      logicalMeter.get().pk.id,
      context().organisationId()
    ))
      .extracting(gme -> gme.gateway.serial, gme -> gme.lastSeen.toInstant())
      .containsExactlyInAnyOrder(
        tuple(GATEWAY_SERIAL_1, CREATED.plusDays(1).toInstant())
      );
  }

  @Test
  @Transactional
  public void gatewayMeterIsNotUpdatedWithDateForHistoricalData() {
    LocalDateTime when = CREATED.toLocalDateTime();

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when, 1.0))
    ));

    measurementMessageConsumer.accept(new MeteringMeasurementMessageDto(
      new IdDto(GATEWAY_SERIAL_1),
      new IdDto(METER_ID_1),
      new IdDto(FACILITY),
      context().defaultOrganisation().externalId,
      SOURCE_SYSTEM_ID,
      singletonList(newValueDto(when.minusDays(1), 2.0))
    ));

    commitTransaction();

    assertThat(gatewayJpaRepository.findAll())
      .hasSize(1)
      .extracting(g -> g.serial)
      .containsExactlyInAnyOrder(GATEWAY_SERIAL_1);

    var logicalMeter = logicalMeterJpaRepository.findBy(context().organisationId(), FACILITY);
    assertThat(logicalMeter).isPresent();

    assertThat(gatewaysMetersJpaRepository.findByLogicalMeterIdAndOrganisationId(
      logicalMeter.get().pk.id,
      context().organisationId()
    ))
      .extracting(gme -> gme.gateway.serial, gme -> gme.lastSeen.toInstant())
      .containsExactlyInAnyOrder(
        tuple(GATEWAY_SERIAL_1, CREATED.toInstant())
      );
  }

  private ValueDto newValueDto(LocalDateTime when, double value) {
    return new ValueDto(when, value, "kWh", "Energy");
  }

  /**
   * Still a mystery why this is needed, use of both jpa and jooq in the same update transaction
   * but jooq is executed as sql through the entity manager.
   */
  private static void commitTransaction() {
    TestTransaction.flagForCommit();
    TestTransaction.end();
  }
}
