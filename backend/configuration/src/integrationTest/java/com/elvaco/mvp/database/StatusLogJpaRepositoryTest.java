package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

public class StatusLogJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @After
  public void tearDown() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();

    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void duplicateMeterLogsAreRejected() {
    UUID meterId = randomUUID();
    physicalMeterJpaRepository.save(
      new PhysicalMeterEntity(
        meterId,
        context().organisationEntity,
        "",
        "abc",
        "",
        "",
        null,
        0,
        emptySet()
      )
    );

    ZonedDateTime start = ZonedDateTime.now();
    physicalMeterStatusLogJpaRepository.save(
      new PhysicalMeterStatusLogEntity(null, meterId, StatusType.OK, start, null)
    );

    assertThatThrownBy(() ->
      physicalMeterStatusLogJpaRepository.save(
        new PhysicalMeterStatusLogEntity(null, meterId, StatusType.OK, start, null)
      )).hasMessageContaining("constraint");
  }

  @Test
  public void duplicateGatewayLogsAreRejected() {
    UUID gatewayId = randomUUID();
    gatewayJpaRepository.save(new GatewayEntity(
      gatewayId,
      context().getOrganisationId(),
      "",
      "",
      emptySet()
    ));

    ZonedDateTime start = ZonedDateTime.now();
    gatewayStatusLogJpaRepository.save(new GatewayStatusLogEntity(
      null,
      gatewayId,
      StatusType.OK,
      start,
      null
    ));

    assertThatThrownBy(() ->
      gatewayStatusLogJpaRepository.save(new GatewayStatusLogEntity(
        null,
        gatewayId,
        StatusType.OK,
        start,
        null
      ))
    ).hasMessageContaining("constraint");
  }

}
