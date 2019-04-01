package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayPk;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterPk;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

public class StatusLogJpaRepositoryTest extends IntegrationTest {

  @After
  public void tearDown() {
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void duplicateMeterLogsAreRejected() {
    UUID meterId = randomUUID();
    physicalMeterJpaRepository.save(
      new PhysicalMeterEntity(
        meterId,
        context().organisationId(),
        "",
        "abc",
        "",
        "",
        null,
        null,
        0,
        1,
        1,
        emptySet(),
        emptySet()
      )
    );

    ZonedDateTime start = ZonedDateTime.now();

    var pk = new PhysicalMeterPk(meterId, context().organisationId());

    physicalMeterStatusLogJpaRepository.save(
      new PhysicalMeterStatusLogEntity(null, pk, StatusType.OK, start, null)
    );

    assertThatThrownBy(() ->
      physicalMeterStatusLogJpaRepository.save(
        new PhysicalMeterStatusLogEntity(null, pk, StatusType.OK, start, null)
      )).hasMessageContaining("constraint");
  }

  @Test
  public void duplicateGatewayLogsAreRejected() {
    UUID gatewayId = randomUUID();
    gatewayJpaRepository.save(new GatewayEntity(
      new EntityPk(gatewayId, context().organisationId()),
      "",
      "",
      "",
      "",
      emptySet(),
      new JsonField()
    ));

    ZonedDateTime start = ZonedDateTime.now();
    gatewayStatusLogJpaRepository.save(new GatewayStatusLogEntity(
      null,
      new GatewayPk(gatewayId, context().organisationId()), StatusType.OK,
      start,
      null
    ));

    assertThatThrownBy(() ->
      gatewayStatusLogJpaRepository.save(new GatewayStatusLogEntity(
        null,
        new GatewayPk(gatewayId, context().organisationId()), StatusType.OK,
        start,
        null
      ))
    ).hasMessageContaining("constraint");
  }
}
